org.springframework.cloud.contract.spec.Contract.make {
    description """
        获取单个数据资产的详细信息
        当客户端请求获取特定资产ID的详情时，服务端应返回该资产的完整信息
    """
    
    request {
        method 'GET'
        url '/api/v1/assets/test-asset-001'
        headers {
            contentType('application/json')
            header('Authorization', value(client('Bearer token')))
        }
    }
    
    response {
        status 200
        body([
            id: 'test-asset-001',
            name: value(consumer('Test Asset')),
            assetType: value(consumer('TABLE')),
            description: value(consumer('测试数据资产描述')),
            owner: value(consumer('admin')),
            sensitivityLevel: value(consumer('INTERNAL')),
            status: value(consumer('ACTIVE')),
            createdAt: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'))),
            updatedAt: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'))),
            tags: value(consumer([])),
            metadata: value(consumer([:]))
        ])
        headers {
            contentType('application/json')
        }
    }
}
