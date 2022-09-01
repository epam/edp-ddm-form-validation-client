/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.integration.formprovider.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.integration.formprovider.config.WireMockConfig;
import com.epam.digital.data.platform.integration.formprovider.dto.ComponentsDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormDto;
import com.epam.digital.data.platform.integration.formprovider.exception.BadRequestException;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.ByteStreams;
import feign.FeignException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableAutoConfiguration
@EnableFeignClients(clients = FormManagementProviderClient.class)
@SpringBootTest(classes = {WireMockConfig.class})
public class FormManagementProviderClientIT {

  @Autowired
  private WireMockServer restClientWireMock;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private FormManagementProviderClient formManagementProviderClient;

  @Test
  public void testFormDataValidationWithValidData() throws JsonProcessingException {
    var formDataDto = FormDataDto.builder().data(new LinkedHashMap<>()).build();
    mockFormDataValidation(200, formDataDto, objectMapper.writeValueAsString(formDataDto));

    var result = formManagementProviderClient.validateFormData("formId", formDataDto);

    assertThat(result).isEqualTo(formDataDto);
  }

  @Test
  public void testFormDataValidationWithInvalidData() throws Exception {
    var errorValidationResponse = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/error_validation_response.json"))));
    var formDataDto = FormDataDto.builder().data(new LinkedHashMap<>()).build();
    mockFormDataValidation(400, formDataDto, errorValidationResponse);

    var ex = assertThrows(BadRequestException.class,
        () -> formManagementProviderClient.validateFormData("formId", formDataDto));

    assertThat(ex).isNotNull();
    assertThat(ex.getErrors().getErrors().size()).isEqualTo(2);
    assertThat(ex.getErrors().getErrors().get(0).getField()).isEqualTo("name");
    assertThat(ex.getErrors().getErrors().get(1).getField()).isEqualTo("edrpou");
  }

  @Test
  public void testFormDataValidationWithInternalServerError() throws Exception {
    var formDataDto = FormDataDto.builder().data(new LinkedHashMap<>()).build();
    int status = 500;
    mockFormDataValidation(status, formDataDto, null);

    var ex = assertThrows(FeignException.class,
        () -> formManagementProviderClient.validateFormData("formId", formDataDto));

    assertThat(ex).isNotNull();
    assertThat(ex.status()).isEqualTo(status);
  }

  @Test
  public void testGetFormById() throws JsonProcessingException {
    var componentsDtos = List.of(
        new ComponentsDto("fileField", "file", true, null, "application/png", "50MB", null));
    var formDto = new FormDto(componentsDtos);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/formId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(formDto))
            )
        ));

    var result = formManagementProviderClient.getForm("formId");

    assertThat(result).isEqualTo(formDto);
  }

  private void mockFormDataValidation(int respStatus, FormDataDto reqBody, String respBody)
      throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/formId/submission"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(reqBody)))
            .withQueryParam("dryrun", equalTo("1"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(respStatus)
                .withBody(respBody)
            )
        ));
  }
}
