package com.example.nexusa.Model;

import lombok.Data;

import java.util.List;

@Data
public class CMENode {
    private String nodeType;      // VOLUME or ENTRY
    private String title;
    private Long startYear;
    private Long endYear;
    private List<ContentBlock> blocks;  // only for ENTRY type
}