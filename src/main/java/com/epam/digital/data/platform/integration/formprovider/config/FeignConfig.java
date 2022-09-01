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

package com.epam.digital.data.platform.integration.formprovider.config;

import com.epam.digital.data.platform.integration.formprovider.decoder.FormValidationErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * The class represents a configuration for feign client.
 */
public class FeignConfig {

  /**
   * Returns error decoder {@link FormValidationErrorDecoder}
   *
   * @return error decoder for form management provider client
   */
  @Bean
  public FormValidationErrorDecoder formValidationDecoder(ObjectMapper objectMapper) {
    return new FormValidationErrorDecoder(objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
