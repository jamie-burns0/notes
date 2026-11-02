# red hat exam preparation notes

## exam guide pdf

- https://access.redhat.com/community/learn/remote-exams


## latest iso for usb boot drive

- https://red.ht/rhrex


## exam history

- we can schedule an exam we have enrolled in
- https://rhtapps.redhat.com/individualexamscheduler/#/Dashboard


## exam preparation

__Copilot prompt__

i have completed the course Red Hat OpenShift Developer II: Building and Deploying Cloud-native Applications v4.18 DO288 and want to prepare for the certification exam EX288. I have a running instance of OpenShift in the Red Hat Developer sandbox.

__Copilot response__

Given your background with Quarkus, OpenShift, Helm, Tekton, and Kubernetes concepts, you're already beyond the level of many first-time EX288 candidates. The challenge now is less about learning features and more about executing quickly and accurately under exam conditions.

A practice approach that works well is:

Pick one scenario.
Set a 30-45 minute timer.
Build everything from scratch in a fresh project.
Delete the project.
Repeat until the workflow feels automatic.

Once you're comfortable, add self-inflicted failures such as:

Incorrect probe paths
Missing secrets
Bad ConfigMap values
Wrong container ports
Broken routes
Failed rollouts
Resource limits that cause OOM kills

Being able to recover from those quickly is often what separates a pass from a fail in a performance-based exam.

Good luck with your EX288 preparation, and enjoy using the Developer Sandbox as your personal exam lab. It is an excellent environment for building the muscle memory you'll need on exam day

# EX288 Practice Scenarios for OpenShift Developer Sandbox

These scenarios are designed to simulate the types of tasks and troubleshooting activities you may encounter while preparing for the Red Hat Certified OpenShift Application Developer Exam (EX288).

The goal is to practice delivering complete solutions rather than memorizing commands.

---

# Scenario 1: Legacy Spring Boot Migration

## Business Requirement

A development team has provided a Spring Boot application in Git.

Deploy it into a new project called:

```text
modernization-dev
```

Requirements:

1. Build directly from source.
2. Use OpenShift S2I.
3. Application configuration must come from a ConfigMap.
4. Database credentials must come from a Secret.
5. Expose the application externally.
6. Configure:
   - liveness probe
   - readiness probe
7. Scale to 3 replicas.
8. Rolling updates must have zero downtime.

## Deliverables

Verify:

```bash
oc get deployment
oc get pods
oc get svc
oc get route
oc describe deployment
```

## Skills Tested

- Projects
- Source-to-Image (S2I)
- ConfigMaps
- Secrets
- Routes
- Probes
- Scaling

---

# Scenario 2: Quarkus Production Deployment

## Business Requirement

Deploy a Quarkus application from Git.

Use:

```text
quarkus-prod
```

Requirements:

1. Build using OpenShift Builds.
2. Runtime image must come from an ImageStream.
3. Application must expose:
   - /q/health/live
   - /q/health/ready
   - /q/health/started
4. Configure:
   - startupProbe
   - readinessProbe
   - livenessProbe
5. Create a Route.
6. Enable resource limits.

CPU:

```text
request: 100m
limit: 500m
```

Memory:

```text
request: 256Mi
limit: 512Mi
```

## Validation

Simulate a failed deployment:

```bash
oc set env deploy/myapp DB_HOST=invalid
```

Demonstrate that OpenShift detects and responds appropriately.

## Skills Tested

- Builds
- ImageStreams
- Health Checks
- Startup Probes
- Resource Limits
- Troubleshooting

---

# Scenario 3: Secure API Deployment

## Business Requirement

Deploy a REST API that requires:

- API key authentication
- Database credentials
- TLS-secured ingress

Requirements:

1. Store credentials in Secrets.
2. Pods must not contain plaintext secrets.
3. Expose the API using an edge TLS Route.
4. Rotate credentials without rebuilding images.

## Tasks

Rotate:

```text
API_KEY
```

Observe and verify deployment behaviour.

## Skills Tested

- Secrets
- Environment Variable Injection
- TLS Routes
- Rolling Updates

---

# Scenario 4: Blue-Green Deployment

## Business Requirement

Version 1 of an application is running in production.

Deploy Version 2.

Requirements:

1. Keep Version 1 available.
2. Deploy Version 2 alongside it.
3. Switch production traffic.
4. Roll back when requested.

Example:

```text
green-app-v1
green-app-v2
```

Switch routing from:

```text
v1
```

to:

```text
v2
```

and back again.

## Skills Tested

- Routes
- Services
- Blue-Green Deployments
- Rollbacks

---

# Scenario 5: Multi-Tier Application

## Business Requirement

Deploy:

```text
frontend
backend
database
```

Requirements:

1. Frontend communicates with Backend.
2. Backend communicates with PostgreSQL.
3. Use Secrets for credentials.
4. Database must not be externally exposed.
5. Backend should only be available internally.
6. Data must be persistent.

Create project:

```text
frontend-project
```

## Skills Tested

- Services
- Internal Networking
- Secrets
- PVCs
- Multi-Tier Architectures

---

# Scenario 6: Broken Deployment Recovery

## Business Requirement

An application is failing in production.

Project:

```text
troubleshoot-app
```

Observed Symptoms:

- Route returns 503
- Pod continuously restarts
- Health probes fail

Determine:

1. Root cause
2. Corrective action
3. Validation of successful recovery

Possible causes include:

- Incorrect container port
- Invalid environment variable
- Missing Secret
- Failed ConfigMap injection
- Wrong probe path
- Insufficient resources

## Recommended Commands

```bash
oc logs
oc describe pod
oc get events
oc rollout history
oc rollout undo
```

## Skills Tested

- Troubleshooting
- Log Analysis
- Event Analysis
- Rollback Procedures

---

# Scenario 7: CI/CD Challenge

## Business Requirement

Create an automated deployment pipeline.

Pipeline requirements:

1. Clone source code.
2. Build container image.
3. Push image.
4. Deploy application.

Use:

```text
Pipeline
Task
PipelineRun
Workspace
Secret
```

### Bonus Challenge

Automatically trigger deployments when source code changes.

## Skills Tested

- Tekton Pipelines
- Service Accounts
- Workspaces
- CI/CD Automation

---

# Scenario 8: 90-Minute Mock Exam

## Challenge

Complete all tasks within:

```text
90 minutes
```

No notes.

No documentation.

Only:

```bash
oc
```

and the OpenShift Web Console.

---

## Part A

Deploy a Quarkus application from Git.

Requirements:

- Project
- Build
- Service
- Route
- Probes

---

## Part B

Create:

- ConfigMap
- Secret

Inject values using environment variables.

---

## Part C

Create a PersistentVolumeClaim.

Requirements:

```text
Size: 1Gi
Mount Path: /data
```

---

## Part D

Deploy a new application version.

Verify:

- Successful rollout
- No downtime

---

## Part E

Break the deployment intentionally.

Example:

```bash
oc set env deployment/app QUARKUS_DATASOURCE_URL=badvalue
```

Recover normal operation.

---

## Skills Tested

- Source Builds
- Deployments
- Routes
- Probes
- Secrets
- ConfigMaps
- PVCs
- Rollouts
- Troubleshooting

---

# Recommended EX288 Study Focus Areas

Prioritize repeated practice in:

1. OpenShift Builds and BuildConfigs
2. ImageStreams
3. Deployments and DeploymentConfigs
4. Secrets and ConfigMaps
5. Health Probes
6. Resource Requests and Limits
7. Persistent Storage
8. Routes and TLS
9. Rollbacks
10. Application Troubleshooting

The objective is to be able to complete each scenario from memory under time pressure and recover quickly from mistakes.
