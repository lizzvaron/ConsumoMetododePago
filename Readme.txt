# 🧪 Automatización API Wompi – Transacciones PSE

Proyecto de pruebas funcionales automatizadas para la integración con la API de Wompi usando el método de pago **PSE**, construido sobre el patrón de diseño **Screenplay** con **Serenity BDD** y **Cucumber**.

---

## 📋 Tabla de Contenidos

- [Descripción](#descripción)
- [Tecnologías y Dependencias](#tecnologías-y-dependencias)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Estructura de Directorios](#estructura-de-directorios)
- [Configuración](#configuración)
- [Cómo Ejecutar](#cómo-ejecutar)
- [Escenarios de Prueba](#escenarios-de-prueba)
- [Patrón Screenplay](#patrón-screenplay)
- [Generación de Firma](#generación-de-firma)
- [Reporte Serenity](#reporte-serenity)

---

## Descripción

Este proyecto automatiza la validación de la integración con la [API de Wompi](https://docs.wompi.co/docs/colombia/inicio-rapido/) para el método de pago PSE. Cubre escenarios exitosos y alternos, verificando el ciclo completo de una transacción: obtención del token de aceptación, generación de firma de integridad SHA-256 y creación de la transacción.

**Ambiente:** UAT Sandbox  
**URL Base:** `https://api-sandbox.co.uat.wompi.dev/v1`

---

## Tecnologías y Dependencias

| Herramienta | Versión | Uso |
|---|---|---|
| Java | 11+ | Lenguaje base |
| Gradle | 9.x | Build y gestión de dependencias |
| Serenity BDD | 4.2.34 | Framework de pruebas y reportes |
| Cucumber | 7.14.0 | BDD / Gherkin |
| REST Assured | 5.4.0 | Llamadas HTTP al API |
| JUnit 5 | 5.10.0 | Motor de ejecución |

### `build.gradle` relevante

```groovy
plugins {
    id 'java'
    id 'net.serenity-bdd.serenity-gradle-plugin' version '4.2.34'
}

dependencies {
    testImplementation "net.serenity-bdd:serenity-core:4.2.34"
    implementation "net.serenity-bdd:serenity-screenplay:4.2.34"
    implementation "net.serenity-bdd:serenity-screenplay-rest:4.2.34"
    testImplementation "io.rest-assured:rest-assured:5.4.0"
    testImplementation "io.cucumber:cucumber-java:7.14.0"
    testImplementation "io.cucumber:cucumber-junit-platform-engine:7.14.0"
}
```

---

## Arquitectura del Proyecto

El proyecto sigue el patrón **Screenplay**, que organiza las pruebas en capas de responsabilidad única:

```
Feature (Gherkin)
      │
      ▼
   Runner  ──────────────────────────────► Configura Cucumber + Serenity
      │
      ▼
StepDefinitions  ──────────────────────► Traduce pasos Gherkin a Tasks
      │
      ├──► GetMerchantTask  ───────────► GET /merchants → guarda acceptance_token
      │
      ├──► CreateTransactionTask  ────► Genera firma → POST /transactions
      │
      └──► SignatureUtil  ────────────► SHA-256(ref + amount + currency + key)
```

### Flujo de una transacción PSE exitosa

```
1. GET /merchants/{pub_key}
        └─► Extrae acceptance_token y lo guarda en memoria del Actor

2. SHA-256(reference + amount_in_cents + currency + integrity_key)
        └─► Genera firma de integridad

3. POST /transactions
        └─► Body: reference, amount, currency, acceptance_token, signature, payment_method{PSE}
        └─► Respuesta esperada: HTTP 201 - status: PENDING
```

---

## Estructura de Directorios

```
src/
├── main/java/payment/
│   ├── task/
│   │   ├── GetMerchantTask.java          # Obtiene acceptance_token del merchant
│   │   └── CreateTransactionTask.java    # Crea la transacción PSE
│   └── utils/
│       └── SignatureUtil.java            # Genera firma SHA-256 de integridad
│
└── test/
    ├── java/payment/
    │   ├── runner/
    │   │   └── PaymentRunner.java        # Configura y orquesta Cucumber
    │   └── stepdefinitions/
    │       └── PaymentStepDefinition.java # Implementa pasos Gherkin
    └── resources/
        └── features/
            └── payment.feature           # Escenarios BDD en Gherkin
```

---

## Configuración

### Llaves de prueba (UAT Sandbox)

| Llave | Valor |
|---|---|
| Pública | `pub_stagtest_g2u0HQd3ZMh05hsSgTS2lUV8t3s4mOt7` |
| Privada | `prv_stagtest_5i0ZGIGiFcDQifYsXxvsny7Y37tKqFWg` |
| Eventos | `stagtest_events_2PDUmhMywUkvb1LvxYnayFbmofT7w39N` |
| **Integridad** | `stagtest_integrity_nAIBuqayW70XpUqJS4qf4STYiISd89Fp` |

> ⚠️ La llave de integridad debe usarse **completa con su prefijo** `stagtest_integrity_...` al generar la firma SHA-256.

### `serenity.conf`

```hocon
restassured {
  baseUri = "https://api-sandbox.co.uat.wompi.dev/v1"
}
```

---

## Cómo Ejecutar

### Prerrequisitos

- Java 11 o superior instalado
- Gradle disponible (o usar el wrapper `./gradlew`)

### Ejecutar todas las pruebas

```bash
./gradlew test
```

### Ejecutar y generar reporte Serenity

```bash
./gradlew test aggregate
```

### Ejecutar una prueba específica por tag

```bash
./gradlew test -Dcucumber.filter.tags="@pse"
```

---

## Escenarios de Prueba

### ✅ Casos Exitosos

| ID | Escenario |
|---|---|
| PSE-001 | Transacción PSE exitosa con datos válidos → HTTP 201 |
| PSE-002 | Obtención dinámica del acceptance_token del merchant |
| PSE-003 | Generación correcta de firma SHA-256 |
| PSE-004 | Transacción con persona jurídica (user_type: 1) |
| PSE-005 | Merchant retorna PSE en métodos de pago aceptados |
| PSE-006 | Transacción con monto mínimo válido |

### ❌ Casos Alternos / Negativos

| ID | Escenario | HTTP Esperado |
|---|---|---|
| PSE-ALT-001 | Firma de integridad inválida | 422 |
| PSE-ALT-002 | Sin acceptance_token en el body | 422 |
| PSE-ALT-003 | Llave privada incorrecta en Authorization | 401 |
| PSE-ALT-004 | amount_in_cents igual a cero | 422 |
| PSE-ALT-005 | Referencia de transacción duplicada | 422/409 |
| PSE-ALT-006 | Código de institución financiera inválido | 422 |
| PSE-ALT-007 | Currency no soportada (USD) | 422 |
| PSE-ALT-008 | Email de cliente con formato inválido | 422 |

---

## Patrón Screenplay

Cada acción del flujo de prueba está encapsulada en una **Task** que el **Actor** ejecuta, siguiendo el principio de responsabilidad única:

```java
// StepDefinition
actor.attemptsTo(
    GetMerchantTask.execute(),      // Obtiene token
    CreateTransactionTask.execute() // Crea transacción
);

// Verificación
assertThat(SerenityRest.lastResponse().statusCode(), equalTo(201));
```

**Ventajas del patrón Screenplay en este proyecto:**
- Cada Task es reutilizable e independiente
- El Actor recuerda datos entre Tasks (`actor.remember / actor.recall`)
- Fácil extensión para nuevos métodos de pago (NEQUI, CARD, etc.)
- Reportes Serenity detallados por cada paso ejecutado

---

## Generación de Firma

Wompi requiere una firma SHA-256 para validar la integridad de cada transacción. La cadena a firmar es:

```
{reference}{amount_in_cents}{currency}{integrity_key}
```

**Ejemplo:**
```
TEST_1777679785946  +  500000  +  COP  +  stagtest_integrity_nAIBuqayW70XpUqJS4qf4STYiISd89Fp
```

La clase `SignatureUtil` implementa este cálculo:

```java
String data = reference + String.valueOf(amount) + currency + integrityKey;
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
// Convertir a hex string...
```

> ⚠️ Error común: usar solo los caracteres finales de la llave sin el prefijo `stagtest_integrity_`. Siempre pasar la llave **completa**.

---

## Reporte Serenity

Después de ejecutar `./gradlew test aggregate`, el reporte HTML se genera en:

```
target/site/serenity/index.html
```

El reporte incluye detalle de cada paso ejecutado, requests/responses HTTP y resultado por escenario.