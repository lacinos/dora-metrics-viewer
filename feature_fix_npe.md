# Fix NullInjectorError in Ngx-Charts

## Root Cause
The error `NullInjectorError: No provider for EnvironmentInjector!` occurs when interacting with the charts (hovering/tooltip creation). This is caused by `ngx-charts` (specifically its `TooltipService` and `InjectionService`) attempting to dynamically create components using Angular's `DomPortalOutlet` or internal injection mechanisms.

In a fully Standalone Angular application (bootstrapped via `bootstrapApplication`), services provided by `NgModules` (like `NgxChartsModule`) that rely on the module injector hierarchy sometimes fail to resolve the `EnvironmentInjector` correctly when imported only at the component level (`DashboardComponent`), especially if they are creating dynamic content attached to the body or a different view context.

## Proposed Fix
To resolve this, we will ensure that the `NgxChartsModule` providers are available at the root application level. This ensures that the services are instantiated with the correct root environment injector.

We will use `importProvidersFrom` in `app.config.ts` to include `NgxChartsModule`.

## Plan
1.  Modify `frontend/src/app/app.config.ts`.
2.  Import `importProvidersFrom` from `@angular/core`.
3.  Import `NgxChartsModule` from `@swimlane/ngx-charts`.
4.  Add `importProvidersFrom(NgxChartsModule)` to the `providers` array in `appConfig`.

## Verification
1.  Restart the application.
2.  Use the Playwright MCP to navigate to the dashboard.
3.  Interact with the charts (hover/click) to ensure the tooltip appears without crashing the app.
4.  Check browser console for errors.