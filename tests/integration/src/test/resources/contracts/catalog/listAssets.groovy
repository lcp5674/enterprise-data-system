org.springframework.cloud.contract.spec.Contract.make {
    description """
        获取数据资产列表
        当客户端请求获取资产列表时，服务端应返回分页的资产数据
    """
    
    request {
        method 'GET'
        url '/api/v1/assets'
        queryParameters {
            parameter 'page': value(consumer('0'))
            parameter 'size': value(consumer('10'))
        }
        headers {
            contentType('application/json')
            header('Authorization', value(client('Bearer token')))
        }
    }
    
    response {
        status 200
        body([
            content: [
                [
                    id: value(consumer(regex('[a-zA-Z0-9-]+'))),
                    name: value(consumer(regex('.+'))),
                    assetType: value(consumer(anyOf('TABLE', 'FILE', 'API', 'STREAM'))),
                    status: value(consumer(anyOf('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'ACTIVE', 'DEPRECATED', 'ARCHIVED'))),
                    owner: value(consumer(regex('.+')))
                ]
            ],
            totalElements: value(consumer(regex('[0-9]+'))),
            totalPages: value(consumer(regex('[0-9]+'))),
            size: value(consumer('10')),
            number: value(consumer('0')),
            first: value(consumer(true)),
            last: value(consumer(true))
        ])
        headers {
            contentType('application/json')
        }
    }
}
