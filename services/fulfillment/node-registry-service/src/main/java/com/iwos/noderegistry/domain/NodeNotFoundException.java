package com.iwos.noderegistry.domain;

public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(String nodeId) {
        super("Node not found for id " + nodeId);
    }
}
