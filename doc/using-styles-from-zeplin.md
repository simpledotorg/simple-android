(The style of an element on Zeplin can be found in the sidebar of the Zeplin app.)

## Glossary
- Style: style attributes for a UI element, like a text field.
- Component: groups of UI elements, like Toolbar or TextInputLayout.

## Naming convention

All styles are prefixed with `Clinic.V2`.

If a style is named as `body2Tag/grey1` on Zeplin, then it'll be stored as two separate styles: `Body2Tag` and `Body2Tag.Grey1`:

```xml
<style name="Clinic.V2.TextAppearance.Body2Tag">
  <item name="android:textSize">12sp</item>
  <item name="android:fontFamily">sans-serif-medium</item>
  <item name="android:letterSpacing" tools:ignore="NewApi">0.06</item>
  <item name="android:lineSpacingExtra">8sp</item>
</style>

<style name="Clinic.V2.TextAppearance.Body2Tag.Grey1">
  <item name="android:textColor">@color/grey1</item>
</style>

<style name="Clinic.V2.TextAppearance.Body2Tag.Green1">
  <item name="android:textColor">@color/green1</item>
</style>
```

Zeplin isn't very smart when it comes to code generation, so it's not recommended to copy the generated styles directly to our XML. For instance, if a style contains attributes that are only related to the text, it should be used with `android:textAppearance` rather than applying the style to the whole TextView.

If a style also contains non-text related attributes like spacing, background, etc., consider splitting it into two styles. A common example are buttons:

```xml
<style name="Clinic.V2.Button.Solid.WithoutIcon.Blue1">
  <item name="android:background">@drawable/selector_background_button_solid_blue1</item>
</style>

<style name="Clinic.V2.TextAppearance.Button1" parent="TextAppearance.AppCompat.Widget.Button">
  <item name="android:textAllCaps">true</item>
  <item name="android:fontFamily">sans-serif-medium</item>
  <item name="android:textSize">@dimen/textsize_16</item>
  <item name="android:letterSpacing" tools:ignore="NewApi">0.08</item>
  <item name="android:lineSpacingExtra">4sp</item>
</style>

<style name="Clinic.V2.TextAppearance.Button1.White100">
  <item name="android:textColor">@color/white100</item>
</style>
```

Usage:

```xml
<Button
  style="@style/Clinic.V2.Button.Solid.WithoutIcon.Blue1"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:textAppearance="@style/Clinic.V2.TextAppearance.Button1.White100" />
```

Another example is `TextInputLayout`:

```xml
<style name="Clinic.V2.TextInputEditText">
  <item name="android:paddingTop">6dp</item>
  <item name="android:paddingBottom">@dimen/spacing_16</item>
</style>
```

Usage:

```xml
<android.support.design.widget.TextInputLayout
  style="@style/Clinic.V2.TextInputLayout"
  android:layout_width="match_parent"
  android:layout_height="wrap_content">

  <android.support.design.widget.TextInputEditText
    style="@style/Clinic.V2.TextInputEditText"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="@style/Clinic.V2.TextAppearance.Body1Left.Grey0" />
</android.support.design.widget.TextInputLayout>
```

