package com.iwos.noderegistry.api.http;

import com.iwos.noderegistry.application.NodeQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final NodeQueryService nodeQueryService;

    public NodeController(NodeQueryService nodeQueryService) {
        this.nodeQueryService = nodeQueryService;
    }

    @GetMapping
    public List<NodeResponse> listNodes(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active
    ) {
        return nodeQueryService.findNodes(type, city, active);
    }

    @GetMapping("/{nodeId}")
    public NodeResponse getNode(@PathVariable String nodeId) {
        return nodeQueryService.getNode(nodeId);
    }
}
