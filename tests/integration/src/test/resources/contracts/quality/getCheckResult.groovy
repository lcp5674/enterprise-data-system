org.springframework.cloud.contract.spec.Contract.make {
    description """
        获取质量检测结果
        当客户端查询检测结果时，服务端应返回检测的详细结果
    """
    
    request {
        method 'GET'
        url '/api/v1/quality/check/check-001'
        headers {
            contentType('application/json')
            header('Authorization', value(client('Bearer token')))
        }
    }
    
    response {
        status 200
        body([
            success: true,
            data: [
                checkId: value(consumer('check-001')),
                assetId: value(consumer(regex('[a-zA-Z0-9-]+'))),
                totalRules: value(consumer(anyInteger())),
                passedRules: value(consumer(anyInteger())),
                failedRules: value(consumer(anyInteger())),
                qualityScore: value(consumer(regex('[0-9]{1,2}(\\.[0-9])?'))),
                issues: [
                    [
                        ruleType: value(consumer(anyOf('NULL_CHECK', 'UNIQUE_CHECK', 'FORMAT_CHECK'))),
                        field: value(consumer(regex('.+'))),
                        message: value(consumer(regex('.+')))
                    ]
                ],
                status: value(consumer('COMPLETED'))
            ]
        ])
        headers {
            contentType('application/json')
        }
    }
}
