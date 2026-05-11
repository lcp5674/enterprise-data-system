org.springframework.cloud.contract.spec.Contract.make {
    description """
        获取血缘关系图
        当客户端请求血缘关系图时，服务端应返回血缘节点和边信息
    """
    
    request {
        method 'POST'
        url '/api/v1/lineage/graph'
        body([
            centerAssetId: value(consumer('test-asset-001')),
            depth: value(consumer('2'))
        ])
        headers {
            contentType('application/json')
            header('Authorization', value(client('Bearer token')))
        }
    }
    
    response {
        status 200
        body([
            nodes: [
                [
                    id: value(consumer(regex('[a-zA-Z0-9-]+'))),
                    name: value(consumer(regex('.+'))),
                    type: value(consumer(anyOf('TABLE', 'FILE', 'API', 'FIELD')))
                ]
            ],
            edges: [
                [
                    source: value(consumer(regex('[a-zA-Z0-9-]+'))),
                    target: value(consumer(regex('[a-zA-Z0-9-]+'))),
                    type: value(consumer(anyOf('DIRECT', 'TRANSFORM', 'DERIVED')))
                ]
            ],
            depth: value(consumer('2'))
        ])
        headers {
            contentType('application/json')
        }
    }
}
