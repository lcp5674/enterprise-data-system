{{/*
Expand the name of the chart.
*/}}
{{- define "edms.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "edms.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "edms.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "edms.labels" -}}
helm.sh/chart: {{ include "edms.chart" . }}
{{ include "edms.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: edms
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "edms.selectorLabels" -}}
app.kubernetes.io/name: {{ include "edms.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "edms.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
    {{ default (include "edms.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Generate secret name for database
*/}}
{{- define "edms.database.secretName" -}}
{{- printf "%s-database-secret" (include "edms.fullname" .) -}}
{{- end -}}

{{/*
Generate config map name for application
*/}}
{{- define "edms.configMapName" -}}
{{- printf "%s-config" (include "edms.fullname" .) -}}
{{- end -}}

{{/*
Generate service name
*/}}
{{- define "edms.serviceName" -}}
{{- printf "%s-service" (include "edms.fullname" .) -}}
{{- end -}}

{{/*
Generate ingress hostname
*/}}
{{- define "edms.ingress.host" -}}
{{- $domain := .Values.global.domain -}}
{{- $environment := .Values.global.environment -}}
{{- if eq $environment "prod" }}
{{- printf "%s" $domain -}}
{{- else }}
{{- printf "%s.%s" $environment $domain -}}
{{- end }}
{{- end -}}

{{/*
Generate image pull secret name
*/}}
{{- define "edms.imagePullSecretName" -}}
{{- printf "%s-registry-secret" (include "edms.fullname" .) -}}
{{- end -}}

{{/*
Generate volumes for configuration
*/}}
{{- define "edms.config.volumes" -}}
- name: config-volume
  configMap:
    name: {{ include "edms.configMapName" . }}
- name: secrets-volume
  secret:
    secretName: {{ include "edms.database.secretName" . }}
{{- end -}}

{{/*
Generate volume mounts for configuration
*/}}
{{- define "edms.config.volumeMounts" -}}
- name: config-volume
  mountPath: /config
  readOnly: true
- name: secrets-volume
  mountPath: /secrets
  readOnly: true
{{- end -}}

{{/*
Generate environment variables for database connection
*/}}
{{- define "edms.database.env" -}}
- name: DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: db-host
- name: DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: db-port
- name: DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: db-name
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: db-username
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: db-password
{{- end -}}

{{/*
Generate redis connection env
*/}}
{{- define "edms.redis.env" -}}
- name: REDIS_HOST
  value: {{ include "edms.fullname" . }}-redis-master
- name: REDIS_PORT
  value: "6379"
- name: REDIS_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ include "edms.database.secretName" . }}
      key: redis-password
{{- end -}}

{{/*
Generate pod anti-affinity configuration
*/}}
{{- define "edms.podAntiAffinity" -}}
podAntiAffinity:
  preferredDuringSchedulingIgnoredDuringExecution:
  - weight: 100
    podAffinityTerm:
      labelSelector:
        matchExpressions:
        - key: app.kubernetes.io/name
          operator: In
          values:
          - {{ include "edms.name" . }}
      topologyKey: kubernetes.io/hostname
{{- end -}}

{{/*
Generate resource requests and limits
*/}}
{{- define "edms.resources" -}}
{{- if .resources -}}
{{ toYaml .resources }}
{{- else -}}
requests:
  cpu: 100m
  memory: 256Mi
limits:
  cpu: 500m
  memory: 512Mi
{{- end -}}
{{- end -}}