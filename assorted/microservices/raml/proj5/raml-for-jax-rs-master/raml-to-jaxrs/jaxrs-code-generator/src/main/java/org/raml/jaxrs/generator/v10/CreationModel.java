/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.generator.v10;

import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

/**
 * Created by jpbelang on 2017-06-17.
 */
public enum CreationModel {

  NEVER_INLINE {

    @Override
    public boolean isInline(TypeDeclaration t) {
      return false;
    }
  },
  ALWAYS_INLINE {

    @Override
    public boolean isInline(TypeDeclaration t) {
      return true;
    }
  },
  INLINE_FROM_TYPE {

    @Override
    public boolean isInline(TypeDeclaration typeDeclaration) {

      if (typeDeclaration == null) {
        return false;
      } else {
        return TypeUtils.shouldCreateNewClass(typeDeclaration,
                                              typeDeclaration.parentTypes().toArray(new TypeDeclaration[0]));
      }
    }
  };

  public abstract boolean isInline(TypeDeclaration t);
}
