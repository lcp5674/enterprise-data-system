org.springframework.cloud.contract.spec.Contract.make {
    description """
        搜索数据资产
        当客户端提交搜索请求时，服务端应返回匹配的资产列表
    """
    
    request {
        method 'GET'
        url '/api/v1/assets/search'
        queryParameters {
            parameter 'keyword': value(consumer('test'))
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
                    name: value(consumer(regex('.*test.*'))),
                    assetType: value(consumer(anyOf('TABLE', 'FILE', 'API', 'STREAM'))),
                    status: value(consumer(anyOf('DRAFT', 'PENDING_REVIEW', 'APPROVED', 'ACTIVE', 'DEPRECATED', 'ARCHIVED')))
                ]
            ],
            totalElements: value(consumer(regex('[0-9]+'))),
            totalPages: value(consumer(regex('[0-9]+')))
        ])
        headers {
            contentType('application/json')
        }
    }
}
