org.springframework.cloud.contract.spec.Contract.make {
    description """
        触发数据质量检测
        当客户端提交质量检测请求时，服务端应返回检测任务信息
    """
    
    request {
        method 'POST'
        url '/api/v1/quality/check'
        body([
            assetId: value(consumer(regex('[a-zA-Z0-9-]+'))),
            rules: [
                [type: 'NULL_CHECK', enabled: true],
                [type: 'UNIQUE_CHECK', enabled: true]
            ]
        ])
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
                checkId: value(consumer(regex('[a-zA-Z0-9-]+'))),
                assetId: fromRequest().body('$.assetId'),
                totalRules: 2,
                passedRules: value(consumer(anyInteger())),
                failedRules: value(consumer(anyInteger())),
                qualityScore: value(consumer(regex('[0-9]{1,2}(\\.[0-9])?'))),
                issues: [],
                status: value(consumer('COMPLETED')),
                startTime: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}'))),
                endTime: value(consumer(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')))
            ]
        ])
        headers {
            contentType('application/json')
        }
    }
}
