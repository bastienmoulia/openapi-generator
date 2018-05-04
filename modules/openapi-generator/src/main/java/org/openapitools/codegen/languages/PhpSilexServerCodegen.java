/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.languages;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.utils.ModelUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PhpSilexServerCodegen extends DefaultCodegen implements CodegenConfig {
    protected String invokerPackage;
    protected String groupId = "org.openapitools";
    protected String artifactId = "openapi-server";
    protected String artifactVersion = "1.0.0";

    public PhpSilexServerCodegen() {
        super();

        invokerPackage = camelize("OpenAPIServer");
        String packagePath = "OpenAPIServer";
        modelPackage = packagePath + File.separator + "lib" + File.separator + "models";
        apiPackage = packagePath + File.separator + "lib";
        outputFolder = "generated-code" + File.separator + "php-silex";

        // no model, api files
        modelTemplateFiles.clear();
        apiTemplateFiles.clear();

        embeddedTemplateDir = templateDir = "php-silex";

        setReservedWordsLowerCase(
                Arrays.asList(
                        "__halt_compiler", "abstract", "and", "array", "as", "break", "callable", "case", "catch",
                        "class", "clone", "const", "continue", "declare", "default", "die", "do", "echo", "else",
                        "elseif", "empty", "enddeclare", "endfor", "endforeach", "endif", "endswitch", "endwhile",
                        "eval", "exit", "extends", "final", "for", "foreach", "function", "global", "goto", "if",
                        "implements", "include", "include_once", "instanceof", "insteadof", "interface", "isset",
                        "list", "namespace", "new", "or", "print", "private", "protected", "public", "require",
                        "require_once", "return", "static", "switch", "throw", "trait", "try", "unset", "use",
                        "var", "while", "xor")
        );

        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);

        // ref: http://php.net/manual/en/language.types.intro.php
        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "boolean",
                        "int",
                        "integer",
                        "double",
                        "float",
                        "string",
                        "object",
                        "DateTime",
                        "mixed",
                        "number")
        );

        instantiationTypes.put("array", "array");
        instantiationTypes.put("map", "map");

        // ref: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#data-types
        typeMapping = new HashMap<String, String>();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("float", "float");
        typeMapping.put("double", "double");
        typeMapping.put("string", "string");
        typeMapping.put("byte", "int");
        typeMapping.put("boolean", "boolean");
        typeMapping.put("date", "DateTime");
        typeMapping.put("datetime", "DateTime");
        typeMapping.put("file", "string");
        typeMapping.put("map", "map");
        typeMapping.put("array", "array");
        typeMapping.put("list", "array");
        typeMapping.put("object", "object");
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping.put("binary", "string");

        supportingFiles.add(new SupportingFile("README.mustache", packagePath.replace('/', File.separatorChar), "README.md"));
        supportingFiles.add(new SupportingFile("composer.json", packagePath.replace('/', File.separatorChar), "composer.json"));
        supportingFiles.add(new SupportingFile("index.mustache", packagePath.replace('/', File.separatorChar), "index.php"));
        supportingFiles.add(new SupportingFile(".htaccess", packagePath.replace('/', File.separatorChar), ".htaccess"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName()

    {
        return "php-silex";
    }

    @Override
    public String getHelp() {
        return "Generates a PHP Silex server library.";
    }

    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiFileFolder() {
        return (outputFolder + File.separator + apiPackage()).replace('/', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return (outputFolder + File.separator + modelPackage()).replace('/', File.separatorChar);
    }

    @Override
    public String getTypeDeclaration(Schema p) {
        if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return getSchemaType(p) + "[" + getTypeDeclaration(inner) + "]";
        } else if (ModelUtils.isMapSchema(p)) {
            Schema inner = (Schema) p.getAdditionalProperties();
            return getSchemaType(p) + "[string," + getTypeDeclaration(inner) + "]";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    public String getSchemaType(Schema p) {
        String swaggerType = super.getSchemaType(p);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type)) {
                return type;
            } else if (instantiationTypes.containsKey(type)) {
                return type;
            }
        } else {
            type = swaggerType;
        }
        if (type == null) {
            return null;
        }
        return toModelName(type);
    }

    @Override
    public String toDefaultValue(Schema p) {
        return "null";
    }


    @Override
    public String toVarName(String name) {
        // return the name in underscore style
        // PhoneNumber => phone_number
        name = underscore(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // parameter name starting with number won't compile
        // need to escape it by appending _ at the beginning
        if (name.matches("^\\d.*")) {
            name = "_" + name;
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        // model name cannot use reserved keyword
        if (isReservedWord(name)) {
            escapeReservedWord(name); // e.g. return => _return
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove ' to avoid code injection
        return input.replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation op : operationList) {
            String path = new String(op.path);
            String[] items = path.split("/", -1);
            String opsPath = "";
            int pathParamIndex = 0;

            for (int i = 0; i < items.length; ++i) {
                if (items[i].matches("^\\{(.*)\\}$")) { // wrap in {}
                    // camelize path variable
                    items[i] = "{" + camelize(items[i].substring(1, items[i].length() - 1), true) + "}";
                }
            }

            op.path = StringUtils.join(items, "/");
        }

        return objs;
    }

}