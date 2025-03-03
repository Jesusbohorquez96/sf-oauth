# SAMLAssertionGen - Guía de Usuario

## Introducción

SAMLAssertionGen es una herramienta para generar **SAML Assertions** que permiten la autenticación en sistemas que requieren este mecanismo de seguridad. 
Este proyecto está basado en **Maven** y requiere configuraciones previas para su correcta ejecución.

---

## Prerrequisitos

Antes de ejecutar el proyecto, asegúrese de contar con lo siguiente:
- **Java 8 o superior** instalado.
- **Maven** instalado y configurado correctamente.
- Conexión a Internet para descargar dependencias de Maven.

---

## Estructura del Proyecto

El proyecto incluye los siguientes archivos y carpetas:
- **src/** → Contiene el código fuente.
- **pom.xml** → Archivo de configuración de Maven.
- **SAMLAssertion.properties** → Archivo de configuración con los valores necesarios para generar la aserción SAML.

---

## Configuración del Archivo `SAMLAssertion.properties`

Antes de ejecutar el proyecto, edite el archivo **SAMLAssertion.properties** con la siguiente información:

```properties
tokenUrl=https://your-token-url.com
clientId=your-client-id
userId=your-user-id
privateKey=your-private-key
expireInMinutes=15
```

### Descripción de los campos:
- **tokenUrl**: URL donde se solicitará el token.
- **clientId**: Identificador del cliente registrado en la aplicación BizX.
- **userId / userName**: Usuario para el cual se generará la aserción SAML. 
  - Si `userId` está vacío o nulo, se utilizará `userName`.
- **privateKey**: Clave privada que coincide con la clave pública configurada en BizX.
- **expireInMinutes**: Tiempo en minutos de validez de la SAML Assertion.

---

## Generación de la SAML Assertion

Existen **dos formas** de generar la SAML Assertion:

### **Opción 1: Ejecución directa con Maven**

```sh
mvn compile exec:java -Dexec.args="SAMLAssertion.properties"
```

---

### **Opción 2: Compilar y ejecutar el JAR manualmente**

#### **1. Compilar y Empaquetar el Proyecto**

```sh
mvn clean compile package
```

Una vez completado el proceso, se generará una carpeta **target/** con el archivo `SAMLAssertionGen-1.0.0.jar`.

#### **2. Preparar Archivos**

Copia los siguientes archivos al mismo directorio:
- `SAMLAssertionGen-1.0.0.jar`
- `SAMLAssertion.properties`

#### **3. Ejecutar el JAR**

```sh
java -jar SAMLAssertionGen-1.0.0.jar "SAMLAssertion.properties"
```

---

## Resultado Esperado

Si la ejecución es exitosa, en la línea de comandos se mostrará el siguiente mensaje:

```
The generated Signed SAML Assertion is: <SAML Assertion Base64>
```

Si además se obtiene un **OAuth Token**, se mostrará un mensaje similar a:

```
OAuth Token Response:
{"access_token":"eyJ0b2tlbkNvbnRlbnQiOnsiYXBpS2V5IjoiTTJZMVlq...","token_type":"Bearer","expires_in":80985}
```

Para decodificar la SAML Assertion en Base64 y verificar su contenido:

```sh
echo "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNv..." | base64 --decode
```

---

