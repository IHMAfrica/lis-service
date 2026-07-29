{{/*
Expand the name of the chart.
*/}}
{{- define "lis-chart.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Fully qualified app name (max 63 chars for the DNS naming spec).
*/}}
{{- define "lis-chart.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Chart name and version for the chart label.
*/}}
{{- define "lis-chart.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels.
*/}}
{{- define "lis-chart.labels" -}}
helm.sh/chart: {{ include "lis-chart.chart" . }}
{{ include "lis-chart.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels.
*/}}
{{- define "lis-chart.selectorLabels" -}}
app.kubernetes.io/name: {{ include "lis-chart.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Service account name.
*/}}
{{- define "lis-chart.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "lis-chart.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Names of the bundled dependencies and generated objects.
*/}}
{{- define "lis-chart.postgresql.fullname" -}}
{{- printf "%s-postgresql" (include "lis-chart.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "lis-chart.redis.fullname" -}}
{{- printf "%s-redis" (include "lis-chart.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "lis-chart.configMapName" -}}
{{- printf "%s-config" (include "lis-chart.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
The Secret the application reads its sensitive env from: the caller's existing
Secret when set, otherwise the one this chart generates.
*/}}
{{- define "lis-chart.secretName" -}}
{{- if .Values.secrets.existingSecret }}
{{- .Values.secrets.existingSecret }}
{{- else }}
{{- printf "%s-secret" (include "lis-chart.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Resolved database connection: the bundled Postgres service when enabled, else externalDatabase.
*/}}
{{- define "lis-chart.databaseHost" -}}
{{- if .Values.postgresql.enabled }}
{{- include "lis-chart.postgresql.fullname" . }}
{{- else }}
{{- required "externalDatabase.host is required when postgresql.enabled=false" .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{- define "lis-chart.databasePort" -}}
{{- if .Values.postgresql.enabled }}{{ .Values.postgresql.service.port }}{{ else }}{{ .Values.externalDatabase.port }}{{ end }}
{{- end }}

{{- define "lis-chart.databaseName" -}}
{{- if .Values.postgresql.enabled }}{{ .Values.postgresql.auth.database }}{{ else }}{{ .Values.externalDatabase.database }}{{ end }}
{{- end }}

{{- define "lis-chart.databaseUser" -}}
{{- if .Values.postgresql.enabled }}{{ .Values.postgresql.auth.username }}{{ else }}{{ .Values.externalDatabase.username }}{{ end }}
{{- end }}

{{/*
Resolved Redis connection.
*/}}
{{- define "lis-chart.redisHost" -}}
{{- if .Values.redis.enabled }}
{{- include "lis-chart.redis.fullname" . }}
{{- else }}
{{- required "externalRedis.host is required when redis.enabled=false" .Values.externalRedis.host }}
{{- end }}
{{- end }}

{{- define "lis-chart.redisPort" -}}
{{- if .Values.redis.enabled }}{{ .Values.redis.service.port }}{{ else }}{{ .Values.externalRedis.port }}{{ end }}
{{- end }}
