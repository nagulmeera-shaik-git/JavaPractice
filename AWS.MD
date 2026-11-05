How do you reduce Lambda cold starts for a Java function?

Use SnapStart, keep the deployment package small, avoid heavy frameworks at startup (or use lightweight ones like Micronaut or Quarkus), initialize SDK clients outside the handler, and use provisioned concurrency for latency-critical functions.

How do you deploy a new version with zero downtime?
Rolling updates on ECS with health checks, or blue-green with CodeDeploy, shifting traffic gradually and rolling back automatically if alarms fire.