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

{{/*
The pre-existing Secret the application (and the bundled Postgres/Redis) read
their environment from. The chart never creates it.
*/}}
{{- define "lis-chart.secretName" -}}
{{- required "envSecret is required (the pre-existing Secret holding all app env)" .Values.envSecret }}
{{- end }}
