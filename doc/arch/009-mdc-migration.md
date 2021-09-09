# ADR 009: Material Design Components (MDC) Migration

## Status:

Accepted on 2020-04-16

## Context:

We have been using `Theme.Design.*` theme from the start, while that worked perfectly it did not allow us to use new material components because those
are dependent on `Theme.MaterialComponents.*` app theme. Using new material components allows us to have much more control over component theming like
text styles, icon & icon gravity, strokes, corners, etc.

- Right now we are using 3 different kind of buttons to satisfy various design requirements `Button`, `OmegaCenterIconButton` & `PrimarySolidButton`.
- Since all our Figma designs are made using material specs/material components, the design specs for certain components didn’t translate properly to
  appcompat variants or there are no components available in appcompat at all. Moving to MDC will make the design to layout/style much easier.

You can find more information on setting up MDC Android [here](https://material.io/develop/android/docs/getting-started/).

## Decision:

Moving to MDC will allow us to use `MaterialButton` & other material components if needed. You can find more material components supported by Android
and how to theme them [here](https://material.io/components).

With `MaterialButton` we can finally use icon, icon gravity, icon tint. This migration will also open better theming support for material components
such as corner radius, strokes, shapes etc., Since we no longer need 3 types of buttons we will remove
`PrimarySolidButton` & `OmegaCenterIconButton`, we will be using `MaterialButton` in place of `Button`.

These are the changes that occur due to this migration.

### Button tag change:

- We will start using `com.google.android.material.button.MaterialButton` in views instead of `Button`.
- In order to use icon in a `MaterialButton` we will use the `icon` attribute instead of `drawableStart` or `drawableEnd` attr.
- We will use `iconGravity` (`textStart`, `viewStart`, `textEnd`, `viewEnd`) for setting the icon position, `iconTint` for setting the icon color.

### Style change:

- `theme_material.xml` will house the main app theme which now extends `Theme.MaterialComponents.Light.NoActionBar`, some of the new attrs we set in
  app theme.

```
<style name="Clinic.V2.Theme" parent="Theme.MaterialComponents.Light.NoActionBar">
  <item name="materialButtonStyle">@style/Clinic.V2.MaterialButton</item>
  <item name="materialButtonOutlinedStyle">@style/Clinic.V2.OutlineButton</item>
  <item name="borderlessButtonStyle">@style/Clinic.V2.TextButton</item>
  <item name="toolbarStyle">@style/Clinic.V2.ToolbarStyle</item>
</style>
```

These attrs can be directly referenced in the view styles instead of using the entire style tag in the view.

- We have 4 primary button styles that we use in app
  - `Clinic.V2.MaterialButton`
    - `Clinic.V2.TextButton`
    - `Clinic.V2.OutlineButton`
    - `Clinic.Button.Flat`
    - You can then style `MaterialButton` using any of these styles or attr.
    - Filled Button : `?attr/materialButtonStyle` or `@style/Clinic.V2.MaterialButton`
    - Text Button: `attr/borderlessButtonStyle` or `@style/Clinic.V2.TextButton`
    - Outline Button: `?attr/materialButtonOutlinedStyle` or `Clinic.V2.OutlineButton`
    - Un-Elevated/Flat Button: `@style/Clinic.Button.Flat`
- You can extend any of those primary button styles to override certain attrs like color, for example:

```
<style name="Clinic.V2.MaterialButton.Green3">
  <item name="backgroundTint">@color/green3</item>
  <item name="android:textColor">@color/green1</item>
  <item name="iconTint">@color/green1</item>
</style>
```

## Consequences:

- This is a new design theme that we will be moving to, so some of the component styles can be broken either partially or completely because of this
  theme switch.
- Material theme will automatically convert any `Button` to `MaterialButton` internally using custom inflater. So that can lead to some components
  that extend `Button` like `RadioButton` styles being broken.
