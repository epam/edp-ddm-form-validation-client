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

package com.epam.digital.data.platform.integration.formprovider.decoder;

import com.epam.digital.data.platform.integration.formprovider.dto.FileFieldErrorDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormErrorListDto;
import com.epam.digital.data.platform.integration.formprovider.exception.FileFieldValidationException;
import com.epam.digital.data.platform.integration.formprovider.exception.FormValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;

/**
 * The class represents an implementation of {@link ErrorDecoder} error decoder for feign client,
 * that is used to raise exception based on status.
 */
public class FormValidationErrorDecoder implements ErrorDecoder {

  private final ObjectMapper objectMapper;
  private final ErrorDecoder errorDecoder;

  public FormValidationErrorDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.errorDecoder = new Default();
  }

  @SneakyThrows
  @Override
  public Exception decode(String methodKey, Response response) {
    if (response.status() == 422 && methodKey.contains("validateFormData")) {
      return new FormValidationException(objectMapper
          .readValue(Util.toByteArray(response.body().asInputStream()), objectMapper.constructType(
              FormErrorListDto.class)));
    }
    if (response.status() == 422) {
      return new FileFieldValidationException(objectMapper
          .readValue(Util.toByteArray(response.body().asInputStream()), objectMapper.constructType(
              FileFieldErrorDto.class)));
    }
    return errorDecoder.decode(methodKey, response);
  }
}
