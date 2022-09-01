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

package com.epam.digital.data.platform.integration.formprovider.exception;

import com.epam.digital.data.platform.integration.formprovider.dto.FormErrorListDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The class represents an exception which will be thrown in case the form data didn't pass
 * validation.
 */
@Getter
@RequiredArgsConstructor
public class BadRequestException extends RuntimeException {

  private final FormErrorListDto errors;
}
