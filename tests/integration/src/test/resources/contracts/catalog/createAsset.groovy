org.springframework.cloud.contract.spec.Contract.make {
    description """
        创建新的数据资产
        当客户端提交创建资产请求时，服务端应创建资产并返回创建后的信息
    """
    
    request {
        method 'POST'
        url '/api/v1/assets'
        body([
            name: value(consumer('Test Asset')),
            assetType: value(consumer('TABLE')),
            description: value(consumer('测试数据资产描述')),
            owner: value(consumer('admin')),
            sensitivityLevel: value(consumer('INTERNAL'))
        ])
        headers {
            contentType('application/json')
            header('Authorization', value(client('Bearer token')))
        }
    }
    
    response {
        status 201
        body([
            id: value(consumer(regex('[a-zA-Z0-9-]+'))),
            name: fromRequest().body('$.name'),
            assetType: fromRequest().body('$.assetType'),
            description: fromRequest().body('$.description'),
            owner: fromRequest().body('$.owner'),
            sensitivityLevel: fromRequest().body('$.sensitivityLevel'),
            status: value(consumer('DRAFT')),
            createdAt: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'))),
            updatedAt: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')))
        ])
        headers {
            contentType('application/json')
            header('Location', value(consumer(regex('.*/api/v1/assets/[a-zA-Z0-9-]+'))))
        }
    }
}
