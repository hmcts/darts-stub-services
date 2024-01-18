 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.19
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/darts-stub-services.jar /opt/app/
COPY wiremock/mappings /opt/app/wiremock/mappings

EXPOSE 4551
CMD [ "darts-stub-services.jar" ]
