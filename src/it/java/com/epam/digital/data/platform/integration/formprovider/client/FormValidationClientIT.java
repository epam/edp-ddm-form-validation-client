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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.integration.formprovider.config.WireMockConfig;
import com.epam.digital.data.platform.integration.formprovider.dto.FileDataValidationDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormDataValidationDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormFieldListValidationDto;
import com.epam.digital.data.platform.integration.formprovider.exception.SubmissionValidationException;
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
@EnableFeignClients(clients = FormValidationClient.class)
@SpringBootTest(classes = {WireMockConfig.class})
public class FormValidationClientIT {

  @Autowired
  private WireMockServer restClientWireMock;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private FormValidationClient formValidationClient;

  @Test
  public void testFormDataValidationWithValidData() throws JsonProcessingException {
    var formDataDto = FormDataValidationDto.builder().data(new LinkedHashMap<>()).build();
    mockFormDataValidation(200, formDataDto, objectMapper.writeValueAsString(formDataDto));

    var result = formValidationClient.validateFormData("formId", formDataDto);

    assertThat(result).isEqualTo(formDataDto);
  }

  @Test
  public void testFormDataValidationWithInvalidData() throws Exception {
    var errorValidationResponse = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/error_validation_response.json"))));
    var formDataDto = FormDataValidationDto.builder().data(new LinkedHashMap<>()).build();
    mockFormDataValidation(422, formDataDto, errorValidationResponse);

    var ex = assertThrows(SubmissionValidationException.class,
        () -> formValidationClient.validateFormData("formId", formDataDto));

    assertThat(ex).isNotNull();
    assertThat(ex.getErrors().getDetails().getErrors().size()).isEqualTo(2);
    assertThat(ex.getErrors().getDetails().getErrors().get(0).getField()).isEqualTo("name");
    assertThat(ex.getErrors().getDetails().getErrors().get(1).getField()).isEqualTo("edrpou");
  }

  @Test
  public void testFormDataValidationWithInternalServerError() throws Exception {
    var formDataDto = FormDataValidationDto.builder().data(new LinkedHashMap<>()).build();
    int status = 500;
    mockFormDataValidation(status, formDataDto, null);

    var ex = assertThrows(FeignException.class,
        () -> formValidationClient.validateFormData("formId", formDataDto));

    assertThat(ex).isNotNull();
    assertThat(ex.status()).isEqualTo(status);
  }

  @Test
  public void testFileFieldValidationWithInvalidData() throws Exception {
    var errorValidationResponse = new String(ByteStreams
        .toByteArray(Objects.requireNonNull(
            getClass().getResourceAsStream("/json/file_field_error_validation_response.json"))));
    var fileDataDto = FileDataValidationDto.builder().fileName("name").contentType("file")
        .size(100L)
        .documentKey("documentKey").build();
    mockFileFieldValidation(422, fileDataDto, errorValidationResponse);

    var ex = assertThrows(SubmissionValidationException.class,
        () -> formValidationClient.validateFileField("formId", "fieldKey", fileDataDto));

    assertThat(ex).isNotNull();
    assertThat(ex.getErrors().getTraceId()).isEqualTo("traceId");
    assertThat(ex.getErrors().getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(ex.getErrors().getDetails().getErrors().get(0).getMessage()).isEqualTo(
        "The type of the downloaded file is not supported.");
  }

  @Test
  public void testFieldNamesValidationWithInvalidData() throws Exception {
    var requestBody = FormFieldListValidationDto.builder()
        .fields(List.of("field1", "field2"))
        .build();
    var errorValidationResponse = "{\"message\": \"Task form does not have fields with names field1, field2\"}";
    mockValidateFormFields(422, requestBody, errorValidationResponse);

    var ex = assertThrows(SubmissionValidationException.class,
        () -> formValidationClient.checkFieldNames("formId", requestBody));

    assertThat(ex).isNotNull();
    assertThat(ex.getErrors().getMessage()).isEqualTo(
        "Task form does not have fields with names field1, field2");
  }

  private void mockFormDataValidation(int respStatus, FormDataValidationDto reqBody,
      String respBody)
      throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/form-submissions/formId/validate"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(reqBody)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(respStatus)
                .withBody(respBody)
            )
        ));
  }

  private void mockFileFieldValidation(int respStatus, FileDataValidationDto reqBody,
      String respBody)
      throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/form-submissions/formId/fields/fieldKey/validate"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(reqBody)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(respStatus)
                .withBody(respBody)
            )
        ));
  }

  private void mockValidateFormFields(int respStatus, FormFieldListValidationDto reqBody,
      String respBody)
      throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/form-submissions/formId/fields/check"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(reqBody)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(respStatus)
                .withBody(respBody)
            )
        ));
  }
}
