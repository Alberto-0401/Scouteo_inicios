# Guía de Estilos CSS - SCOUTEO

## Resumen de Implementación

Se ha creado un sistema completo de estilos CSS para la aplicación Scouteo, basado en las mejores prácticas del documento "DI - TEMA 2-5 CSS.pdf".

## Archivos Modificados

### 1. Archivo CSS Principal
**Ubicación:** `app/src/main/resources/scouteo.css`

Este archivo contiene todos los estilos de la aplicación:
- Colores del tema (verde deportivo como principal)
- Estilos para todos los componentes JavaFX
- Clases de validación (error, success, warning, info)
- Efectos hover y pressed
- Estilos para tablas
- Utilidades CSS

### 2. Archivos FXML Modificados
Los siguientes archivos FXML han sido actualizados con clases CSS:

- ✅ `Dashboard.fxml` - Aplicados estilos a títulos, tarjetas y botones
- ✅ `FormJugador.fxml` - Aplicados estilos a formulario y validaciones
- ✅ `ListadoJugadores.fxml` - Aplicados estilos a tabla y filtros

### 3. Código Java Modificado
- ✅ `Main.java` - Agregado carga global del CSS

## Paleta de Colores

```css
Verde Principal: #2E7D32 (tema deportivo)
Verde Oscuro: #1B5E20
Verde Claro: #4CAF50
Azul Secundario: #1976D2
Naranja Acento: #FF6F00
Error: #F44336
Success: #4CAF50
Warning: #FF9800
Info: #2196F3
```

## Clases CSS Disponibles

### Para Títulos y Texto
```xml
styleClass="label-title"       <!-- Título grande y destacado -->
styleClass="label-subtitle"    <!-- Subtítulo -->
styleClass="label-header"      <!-- Encabezado de sección -->
styleClass="label-secondary"   <!-- Texto secundario -->
styleClass="label-error"       <!-- Mensaje de error -->
styleClass="label-success"     <!-- Mensaje de éxito -->
styleClass="form-label"        <!-- Label de formulario -->
```

### Para Botones
```xml
<!-- Botón predeterminado (verde) -->
<Button text="Guardar" />

<!-- Botones especiales -->
<Button text="..." styleClass="button-secondary" />  <!-- Azul -->
<Button text="..." styleClass="button-success" />   <!-- Verde -->
<Button text="..." styleClass="button-danger" />    <!-- Rojo -->
<Button text="..." styleClass="button-warning" />   <!-- Naranja -->
<Button text="..." styleClass="button-outline" />   <!-- Transparente con borde -->
```

### Para Validación de Campos
```xml
<!-- TextField con error -->
<TextField styleClass="error" />

<!-- TextField con éxito -->
<TextField styleClass="success" />

<!-- TextField con warning -->
<TextField styleClass="warning" />

<!-- TextField con info -->
<TextField styleClass="info" />
```

### Para Dashboard
```xml
<VBox styleClass="dashboard-card">  <!-- Tarjeta con sombra -->
    <Label styleClass="stat-label" />
    <Label styleClass="stat-value" />
</VBox>
```

### Para Paneles
```xml
<VBox styleClass="panel" />          <!-- Panel con sombra -->
<VBox styleClass="panel-header" />   <!-- Encabezado de panel -->
```

### Utilidades
```xml
<HBox styleClass="text-center" />    <!-- Centrar texto -->
<HBox styleClass="text-right" />     <!-- Alinear a la derecha -->
<VBox styleClass="spacing-small" />  <!-- Spacing de 10px -->
<VBox styleClass="spacing-medium" /> <!-- Spacing de 20px -->
<VBox styleClass="spacing-large" />  <!-- Spacing de 30px -->
<VBox styleClass="padding-small" />  <!-- Padding de 10px -->
<VBox styleClass="padding-medium" /> <!-- Padding de 20px -->
<VBox styleClass="padding-large" />  <!-- Padding de 30px -->
```

## Cómo Aplicar CSS a Nuevos Archivos FXML

### Opción 1: Desde el FXML (Recomendado)
```xml
<VBox xmlns:fx="http://javafx.com/fxml/1"
      stylesheets="@../scouteo.css">
    <!-- Tu contenido -->
</VBox>
```

### Opción 2: Desde el Código Java
```java
scene.getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
```

### Opción 3: Aplicar clases CSS a componentes
```xml
<Label text="Título" styleClass="label-title"/>
<Button text="Guardar" styleClass="button-success"/>
<TextField fx:id="txtNombre" styleClass="error"/>
```

## Validación de Campos con CSS (Desde Java)

Según el PDF, puedes aplicar estilos dinámicamente desde Java:

```java
// Aplicar clase de error
nombreTextField.getStyleClass().add("error");

// Quitar clase de error
nombreTextField.getStyleClass().remove("error");

// Aplicar clase de éxito
nombreTextField.getStyleClass().add("success");

// Aplicar estilo directo
campo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
```

## Integración con ControlsFX (Validaciones)

El CSS ya está diseñado para trabajar con las validaciones de ControlsFX:

```java
// Escuchar cambios en el resultado de validación
validationSupport.validationResultProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue.getErrors().isEmpty() && newValue.getWarnings().isEmpty()) {
        control.getStyleClass().removeAll("error", "warning");
        control.getStyleClass().add("success");
    } else if (!newValue.getErrors().isEmpty()) {
        control.getStyleClass().removeAll("success", "warning");
        control.getStyleClass().add("error");
    } else if (!newValue.getWarnings().isEmpty()) {
        control.getStyleClass().removeAll("success", "error");
        control.getStyleClass().add("warning");
    }
});
```

## Efectos Incluidos

### Botones
- `:hover` - Botón se agranda ligeramente y cambia de color
- `:pressed` - Botón se reduce ligeramente
- `:disabled` - Botón con opacidad reducida

### TextFields
- `:focused` - Borde verde y sombra al enfocar
- Validación con colores (error rojo, success verde, warning naranja)

### TablaView
- Filas alternas con colores diferentes
- Hover sobre filas
- Selección de filas
- Encabezados con fondo verde

## Verificar que el CSS se está Aplicando

1. **Ejecutar la aplicación** y verificar visualmente los cambios
2. **Abrir SceneBuilder** y previsualizar los FXML con el CSS
3. **Verificar en consola** si hay errores de carga del CSS

### Solución de Problemas

Si no ves los estilos:

1. **Verificar la ruta del CSS** en el FXML:
   ```xml
   stylesheets="@../scouteo.css"
   ```

2. **Verificar que el CSS esté en resources:**
   ```
   app/src/main/resources/scouteo.css
   ```

3. **Limpiar y recompilar:**
   ```bash
   ./gradlew clean build
   ```

4. **Verificar en Main.java** que se carga el CSS:
   ```java
   scene.getStylesheets().add(getClass().getResource("/scouteo.css").toExternalForm());
   ```

## Personalización

Para cambiar los colores del tema, edita las variables en `.root` del archivo CSS:

```css
.root {
    -fx-primary-color: #2E7D32;    /* Cambia este color */
    -fx-secondary-color: #1976D2;   /* Y este */
    /* etc... */
}
```

## Ejemplo Completo de Formulario Estilizado

```xml
<VBox spacing="15" stylesheets="@../scouteo.css" styleClass="form-container">
    <Label text="NUEVO USUARIO" styleClass="label-title"/>
    <Separator/>

    <Label text="Datos Personales" styleClass="label-subtitle"/>

    <VBox spacing="5">
        <Label text="Nombre *" styleClass="form-label"/>
        <TextField fx:id="txtNombre" promptText="Escribe tu nombre"/>
    </VBox>

    <HBox spacing="10" alignment="CENTER_RIGHT">
        <Label fx:id="lblError" styleClass="label-error"/>
        <Button text="Cancelar" styleClass="button-outline"/>
        <Button text="Guardar" styleClass="button-success"/>
    </HBox>
</VBox>
```

## Recursos Adicionales

- **Documentación oficial CSS JavaFX:** https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/doc-files/cssref.html
- **PDF de referencia:** DI - TEMA 2-5 CSS.pdf
- **Repositorio de ejemplos:** https://github.com/MolinaJM/DI-T2-6-CSS

---

**Creado:** Diciembre 2025
**Versión:** 1.0
