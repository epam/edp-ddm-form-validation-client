# ddm-form-provider-client

### Overview

Project with rest client for form provider.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-form-provider-client</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Enable Feign client `@EnableFeignClients(clients = FormManagementProviderClient.class)`
3. Inject`com.epam.digital.data.platform.integration.FormManagementProviderClient` bean.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-form-validation-client is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).