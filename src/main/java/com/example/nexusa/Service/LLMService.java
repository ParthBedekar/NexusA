package com.example.nexusa.Service;

import com.example.nexusa.Dto.GlobalDTOs.LLMChatMessageDTO;
import com.example.nexusa.Dto.GlobalDTOs.LLMChatSessionDTO;
import com.example.nexusa.Dto.GlobalDTOs.LLMCreateSessionRequest;
import com.example.nexusa.Dto.GlobalDTOs.LLMCreateSessionResponse;
import com.example.nexusa.Dto.GlobalDTOs.LLMQueryRequest;
import com.example.nexusa.Dto.GlobalDTOs.LLMQueryResponse;
import com.example.nexusa.Model.CVersion;
import com.example.nexusa.Model.LLMChatMessage;
import com.example.nexusa.Model.LLMChatSession;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.ChatMessageRepository;
import com.example.nexusa.Repository.GlobalRepositories.ChatSessionRepository;
import com.example.nexusa.Repository.GlobalRepositories.CVersionRepository;
import com.example.nexusa.Repository.GlobalRepositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LLMService {
    private static final int MAX_HISTORY_MESSAGES = 20;
    private static final int MAX_SUMMARY_CHARS = 3800;

    private final CVersionRepository cVersionRepository;
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;
    private final LLMProvider llmProvider;
    private final String defaultModel;

    public LLMService(CVersionRepository cVersionRepository,
                      UserRepository userRepository,
                      ChatSessionRepository chatSessionRepository,
                      ChatMessageRepository chatMessageRepository,
                      LLMProvider llmProvider,
                      ObjectMapper objectMapper,
                      @Value("${llm.model:meta-llama/llama-4-scout-17b-16e-instruct}") String defaultModel) {
        this.cVersionRepository = cVersionRepository;
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.llmProvider = llmProvider;
        this.objectMapper = objectMapper;
        this.defaultModel = defaultModel;
    }

    public LLMQueryResponse queryCivilization(UUID civId, LLMQueryRequest request) {
        User user = getAuthenticatedUser();

        CVersion latest = cVersionRepository.findTopByCivilization_CivIdOrderByCommitTimestampDesc(civId)
                .orElseThrow(() -> new RuntimeException("No versions found for this civilization"));

        LLMChatSession session = getOrCreateChatSession(user, civId, request.getSessionId(), request.getModelName());
        List<LLMChatMessage> history = chatMessageRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());

        if (request.getModelName() != null && !request.getModelName().isBlank()) {
            session.setModelName(request.getModelName());
        }

        List<LLMProviderMessage> messages = buildConversationMessages(user, civId, latest.getSerializedTree(), history, request.getQuery());
        String answer = llmProvider.complete(session.getModelName(), messages);

        appendConversationMessage(session, "user", request.getQuery());
        appendConversationMessage(session, "assistant", answer);
        session.setUpdatedAt(LocalDateTime.now());

        if (session.getSessionName() == null || session.getSessionName().isBlank() || "default".equalsIgnoreCase(session.getSessionName()) || "Unnamed chat".equalsIgnoreCase(session.getSessionName())) {
            String generatedName = generateSessionName(session, history, request.getQuery(), answer);
            if (generatedName != null && !generatedName.isBlank()) {
                session.setSessionName(generatedName);
            }
        }

        session = chatSessionRepository.save(session);

        List<LLMChatMessageDTO> responseHistory = toDtos(chatMessageRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId()));
        LLMQueryResponse response = new LLMQueryResponse();
        response.setSessionId(session.getSessionId());
        response.setSessionName(session.getSessionName());
        response.setAnswer(answer);
        response.setConversation(responseHistory);
        return response;
    }

    public List<LLMChatMessageDTO> getConversation(UUID civId, UUID sessionId) {
        User user = getAuthenticatedUser();
        LLMChatSession session = getOrCreateChatSession(user, civId, sessionId, null);
        return toDtos(chatMessageRepository.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId()));
    }

    public List<LLMChatSessionDTO> listSessions(UUID civId) {
        User user = getAuthenticatedUser();
        List<LLMChatSession> sessions = chatSessionRepository.findByCreatedBy_EmailAndCivIdOrderByUpdatedAtDesc(user.getEmail(), civId);
        List<LLMChatSessionDTO> dtos = new ArrayList<>();
        for (LLMChatSession session : sessions) {
            dtos.add(toSessionDto(session));
        }
        return dtos;
    }

    public LLMChatSessionDTO createSession(UUID civId, LLMCreateSessionRequest request) {
        User user = getAuthenticatedUser();
        LLMChatSession session = new LLMChatSession();
        session.setCivId(civId);
        session.setCreatedBy(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setSessionName(request.getSessionName() == null || request.getSessionName().isBlank() ? "Unnamed chat" : request.getSessionName());
        session.setModelName(request.getModelName() == null || request.getModelName().isBlank() ? defaultModel : request.getModelName());
        session.setDefaultSession(false);
        session = chatSessionRepository.save(session);
        return toSessionDto(session);
    }

    public LLMChatSessionDTO renameSession(UUID civId, UUID sessionId, String newName) {
        User user = getAuthenticatedUser();
        LLMChatSession session = chatSessionRepository
                .findBySessionIdAndCreatedBy_Email(sessionId, user.getEmail())
                .orElseThrow(() -> new RuntimeException("Chat session not found for this user."));
        if (newName != null && !newName.isBlank()) session.setSessionName(newName);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        return toSessionDto(session);
    }

    public void deleteSession(UUID civId, UUID sessionId) {
        User user = getAuthenticatedUser();
        LLMChatSession session = chatSessionRepository
                .findBySessionIdAndCreatedBy_Email(sessionId, user.getEmail())
                .orElseThrow(() -> new RuntimeException("Chat session not found for this user."));

        if (!session.getCivId().equals(civId)) {
            throw new RuntimeException("Chat session does not belong to the requested civilization.");
        }

        // delete messages then session
        chatMessageRepository.deleteBySession_SessionId(sessionId);
        chatSessionRepository.delete(session);
    }

    private LLMChatSession getOrCreateChatSession(User user, UUID civId, UUID sessionId, String requestedModel) {
        if (sessionId != null) {
            return chatSessionRepository.findBySessionIdAndCreatedBy_Email(sessionId, user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Chat session not found for this user."));
        }

        Optional<LLMChatSession> existing = chatSessionRepository
                .findByCivIdAndCreatedBy_EmailAndDefaultSessionTrue(civId, user.getEmail());

        if (existing.isPresent()) {
            LLMChatSession session = existing.get();
            if (requestedModel != null && !requestedModel.isBlank() && !requestedModel.equals(session.getModelName())) {
                session.setModelName(requestedModel);
                session.setUpdatedAt(LocalDateTime.now());
                chatSessionRepository.save(session);
            }
            return session;
        }

        return createDefaultSession(user, civId, requestedModel);
    }

    private LLMChatSession createDefaultSession(User user, UUID civId, String modelName) {
        LLMChatSession session = new LLMChatSession();
        session.setCivId(civId);
        session.setCreatedBy(user);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setSessionName("default");
        session.setModelName(modelName == null || modelName.isBlank() ? defaultModel : modelName);
        session.setDefaultSession(true);
        return chatSessionRepository.save(session);
    }

    private List<LLMChatMessageDTO> toDtos(List<LLMChatMessage> conversation) {
        List<LLMChatMessageDTO> result = new ArrayList<>();
        for (LLMChatMessage message : conversation) {
            LLMChatMessageDTO dto = new LLMChatMessageDTO();
            dto.setRole(message.getRole());
            dto.setContent(message.getContent());
            result.add(dto);
        }
        return result;
    }

    private LLMChatSessionDTO toSessionDto(LLMChatSession session) {
        LLMChatSessionDTO dto = new LLMChatSessionDTO();
        dto.setSessionId(session.getSessionId());
        dto.setSessionName(session.getSessionName());
        dto.setModelName(session.getModelName());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }

    private void appendConversationMessage(LLMChatSession session, String role, String content) {
        LLMChatMessage message = new LLMChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);
    }

    private String generateSessionName(LLMChatSession session, List<LLMChatMessage> history, String userQuery, String answer) {
        try {
            List<LLMProviderMessage> messages = new ArrayList<>();
            messages.add(LLMProviderMessage.system("You are a title generator for a chat about a historical civilization knowledge tree."));
            messages.add(LLMProviderMessage.system("Create a short, descriptive title for this conversation in 3-6 words. Use only letters, numbers, and spaces."));
            messages.add(LLMProviderMessage.system("Civilization ID: " + session.getCivId()));
            messages.add(LLMProviderMessage.system("Current model: " + session.getModelName()));
            messages.add(LLMProviderMessage.system("User question: " + userQuery));
            messages.add(LLMProviderMessage.system("Assistant answer: " + answer));
            messages.add(LLMProviderMessage.system("Conversation history:"));

            int count = 0;
            for (LLMChatMessage message : history) {
                if (count++ >= 12) break;
                messages.add(new LLMProviderMessage(message.getRole(), message.getContent()));
            }

            messages.add(LLMProviderMessage.user("Create a concise session title for the conversation above."));
            String name = llmProvider.complete(session.getModelName(), messages);
            if (name != null) {
                name = name.trim();
                if (name.length() > 60) {
                    name = name.substring(0, 60).trim();
                }
                name = name.replaceAll("[^a-zA-Z0-9 ]", "");
            }
            return name;
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<LLMProviderMessage> buildConversationMessages(User user, UUID civId, String serializedTree,
                                                               List<LLMChatMessage> history, String userQuery) {
        List<LLMProviderMessage> messages = new ArrayList<>();
        messages.add(LLMProviderMessage.system("You are an expert assistant for a historical civilization knowledge tree."));
        messages.add(LLMProviderMessage.system("Use only the provided civilization tree information to answer the user's question."));
        messages.add(LLMProviderMessage.system("Do not hallucinate facts outside the available tree."));
        messages.add(LLMProviderMessage.system("Civilization ID: " + civId));
        messages.add(LLMProviderMessage.system("User email: " + user.getEmail()));
        messages.add(LLMProviderMessage.system("Tree summary:\n" + renderTreeSummary(serializedTree)));

        List<LLMChatMessage> recent = history;
        if (recent.size() > MAX_HISTORY_MESSAGES) {
            recent = history.subList(history.size() - MAX_HISTORY_MESSAGES, history.size());
        }

        for (LLMChatMessage message : recent) {
            messages.add(new LLMProviderMessage(message.getRole(), message.getContent()));
        }
        messages.add(LLMProviderMessage.user(userQuery));
        return messages;
    }

    private String renderTreeSummary(String serializedTree) {
        if (serializedTree == null || serializedTree.isBlank()) {
            return "No tree data is available.";
        }

        StringBuilder builder = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(serializedTree);
            summarizeNode(root, builder, 0);
        } catch (Exception e) {
            return "Unable to summarize the tree data.";
        }
        return builder.toString();
    }

    private void summarizeNode(JsonNode node, StringBuilder builder, int depth) {
        if (node == null || builder.length() > MAX_SUMMARY_CHARS) {
            return;
        }

        String indent = "".repeat(Math.min(depth * 2, 20));
        JsonNode data = node.path("data");
        String title = data.path("title").asText("");
        String type = data.path("nodeType").asText(node.path("nodeType").asText("Node"));
        String start = data.has("startYear") ? formatYear(data.path("startYear").asLong()) : "";
        String end = data.has("endYear") ? formatYear(data.path("endYear").asLong()) : "";

        builder.append(indent).append(type);
        if (!title.isBlank()) {
            builder.append(": ").append(title);
        }
        if (!start.isBlank() || !end.isBlank()) {
            builder.append(" (").append(start).append(" - ").append(end).append(")");
        }
        builder.append("\n");

        JsonNode children = node.path("children");
        if (children.isArray()) {
            for (JsonNode child : children) {
                summarizeNode(child, builder, depth + 1);
                if (builder.length() > MAX_SUMMARY_CHARS) {
                    builder.append(indent).append("...more nodes omitted...\n");
                    break;
                }
            }
        }
    }

    private String formatYear(long year) {
        return year < 0 ? Math.abs(year) + " BCE" : year + " CE";
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}