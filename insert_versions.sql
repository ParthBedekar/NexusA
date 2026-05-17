INSERT INTO civilization_versions (version_id, civ_id, hash, serialized_tree, committed_by, commit_message, commit_type, commit_timestamp, start_date, end_date) 
VALUES (gen_random_uuid(), 'cc333333-3333-3333-3333-333333333333'::uuid, md5(random()::text), 
'{"nodeId":"root","data":{"nodeType":"VOLUME","title":"Mughal Timeline"},"children":[{"nodeId":"vol-1","data":{"nodeType":"VOLUME","title":"Early Mughal","startYear":1526,"endYear":1700},"children":[]},{"nodeId":"vol-2","data":{"nodeType":"VOLUME","title":"Later Mughal","startYear":1700,"endYear":1857},"children":[]}]}'::jsonb, 
'e7e748bc-303d-4a54-98ea-c64038ca3c7d'::uuid, 'Initial version', 'MAJOR', now(), 1526, 1857);

INSERT INTO civilization_versions (version_id, civ_id, hash, serialized_tree, committed_by, commit_message, commit_type, commit_timestamp, start_date, end_date) 
VALUES (gen_random_uuid(), 'bb222222-2222-2222-2222-222222222222'::uuid, md5(random()::text), 
'{"nodeId":"root","data":{"nodeType":"VOLUME","title":"Maurya Timeline"},"children":[{"nodeId":"vol-1","data":{"nodeType":"VOLUME","title":"Early Maurya","startYear":-322,"endYear":-200},"children":[]},{"nodeId":"vol-2","data":{"nodeType":"VOLUME","title":"Late Maurya","startYear":-200,"endYear":-185},"children":[]}]}'::jsonb, 
'e7e748bc-303d-4a54-98ea-c64038ca3c7d'::uuid, 'Initial version', 'MAJOR', now(), -322, -185);

INSERT INTO civilization_versions (version_id, civ_id, hash, serialized_tree, committed_by, commit_message, commit_type, commit_timestamp, start_date, end_date) 
VALUES (gen_random_uuid(), 'aa111111-1111-1111-1111-111111111111'::uuid, md5(random()::text), 
'{"nodeId":"root","data":{"nodeType":"VOLUME","title":"Indus Valley Timeline"},"children":[{"nodeId":"vol-1","data":{"nodeType":"VOLUME","title":"Early Period","startYear":-3300,"endYear":-2600},"children":[]},{"nodeId":"vol-2","data":{"nodeType":"VOLUME","title":"Mature Period","startYear":-2600,"endYear":-1900},"children":[]}]}'::jsonb, 
'e7e748bc-303d-4a54-98ea-c64038ca3c7d'::uuid, 'Initial version', 'MAJOR', now(), -3300, -1900);
