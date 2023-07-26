package vn.vietmap.services.android.navigation.ui.v5.route;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import vn.vietmap.services.android.navigation.ui.v5.utils.MapImageUtils;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.style.expressions.Expression;
import vn.vietmap.vietmapsdk.style.layers.LineLayer;
import vn.vietmap.vietmapsdk.style.layers.Property;
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer;

import static vn.vietmap.vietmapsdk.style.expressions.Expression.color;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.exponential;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.get;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.interpolate;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.literal;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.match;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.product;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.stop;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.switchCase;
import static vn.vietmap.vietmapsdk.style.expressions.Expression.zoom;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconImage;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconPitchAlignment;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.iconSize;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineCap;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineColor;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineJoin;
import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.lineWidth;

class MapRouteLayerProvider {

  LineLayer initializeRouteShieldLayer(Style style, float routeScale, float alternativeRouteScale,
                                       int routeShieldColor, int alternativeRouteShieldColor) {
    LineLayer shieldLayer = style.getLayerAs(RouteConstants.ROUTE_SHIELD_LAYER_ID);
    if (shieldLayer != null) {
      style.removeLayer(shieldLayer);
    }

    shieldLayer = new LineLayer(RouteConstants.ROUTE_SHIELD_LAYER_ID, RouteConstants.ROUTE_SOURCE_ID).withProperties(
      lineCap(Property.LINE_CAP_ROUND),
      lineJoin(Property.LINE_JOIN_ROUND),
      lineWidth(
        interpolate(
          exponential(1.5f), zoom(),
          stop(10f, 7f),
          stop(14f, product(literal(10.5f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(16.5f, product(literal(15.5f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(19f, product(literal(24f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(22f, product(literal(29f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale))))
        )
      ),
      lineColor(
        switchCase(
          Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), color(routeShieldColor),
          color(alternativeRouteShieldColor)
        )
      )
    );
    return shieldLayer;
  }

  LineLayer initializeRouteLayer(Style style, boolean roundedLineCap, float routeScale,
                                 float alternativeRouteScale, int routeDefaultColor, int routeModerateColor,
                                 int routeSevereColor, int alternativeRouteDefaultColor,
                                 int alternativeRouteModerateColor, int alternativeRouteSevereColor) {
    LineLayer routeLayer = style.getLayerAs(RouteConstants.ROUTE_LAYER_ID);
    if (routeLayer != null) {
      style.removeLayer(routeLayer);
    }

    String lineCap = Property.LINE_CAP_ROUND;
    String lineJoin = Property.LINE_JOIN_ROUND;
    if (!roundedLineCap) {
      lineCap = Property.LINE_CAP_BUTT;
      lineJoin = Property.LINE_JOIN_BEVEL;
    }

    routeLayer = new LineLayer(RouteConstants.ROUTE_LAYER_ID, RouteConstants.ROUTE_SOURCE_ID).withProperties(
      lineCap(lineCap),
      lineJoin(lineJoin),
      lineWidth(
        interpolate(
          exponential(1.5f), zoom(),
          stop(4f, product(literal(3f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(10f, product(literal(4f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(13f, product(literal(6f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(16f, product(literal(10f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(19f, product(literal(14f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale)))),
          stop(22f, product(literal(18f),
            switchCase(
              Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), literal(routeScale),
              literal(alternativeRouteScale))))
        )
      ),
      lineColor(
        switchCase(
          Expression.get(RouteConstants.PRIMARY_ROUTE_PROPERTY_KEY), match(
            Expression.toString(get(RouteConstants.CONGESTION_KEY)),
            color(routeDefaultColor),
            stop(RouteConstants.MODERATE_CONGESTION_VALUE, color(routeModerateColor)),
            stop(RouteConstants.HEAVY_CONGESTION_VALUE, color(routeSevereColor)),
            stop(RouteConstants.SEVERE_CONGESTION_VALUE, color(routeSevereColor))
          ),
          match(
            Expression.toString(get(RouteConstants.CONGESTION_KEY)),
            color(alternativeRouteDefaultColor),
            stop(RouteConstants.MODERATE_CONGESTION_VALUE, color(alternativeRouteModerateColor)),
            stop(RouteConstants.HEAVY_CONGESTION_VALUE, color(alternativeRouteSevereColor)),
            stop(RouteConstants.SEVERE_CONGESTION_VALUE, color(alternativeRouteSevereColor))
          )
        )
      )
    );
    return routeLayer;
  }

  SymbolLayer initializeWayPointLayer(Style style, Drawable originIcon,
                                      Drawable destinationIcon) {
    SymbolLayer wayPointLayer = style.getLayerAs(RouteConstants.WAYPOINT_LAYER_ID);
    if (wayPointLayer != null) {
      style.removeLayer(wayPointLayer);
    }

    Bitmap bitmap = MapImageUtils.getBitmapFromDrawable(originIcon);
    style.addImage(RouteConstants.ORIGIN_MARKER_NAME, bitmap);
    bitmap = MapImageUtils.getBitmapFromDrawable(destinationIcon);
    style.addImage(RouteConstants.DESTINATION_MARKER_NAME, bitmap);

    wayPointLayer = new SymbolLayer(RouteConstants.WAYPOINT_LAYER_ID, RouteConstants.WAYPOINT_SOURCE_ID).withProperties(
      iconImage(
        match(
          Expression.toString(Expression.get(RouteConstants.WAYPOINT_PROPERTY_KEY)), Expression.literal(RouteConstants.ORIGIN_MARKER_NAME),
          stop(RouteConstants.WAYPOINT_ORIGIN_VALUE, Expression.literal(RouteConstants.ORIGIN_MARKER_NAME)),
          stop(RouteConstants.WAYPOINT_DESTINATION_VALUE, Expression.literal(RouteConstants.DESTINATION_MARKER_NAME))
        )),
      iconSize(
        interpolate(
          exponential(1.5f), zoom(),
          stop(0f, 0.6f),
          stop(10f, 0.8f),
          stop(12f, 1.3f),
          stop(22f, 2.8f)
        )
      ),
      iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
      iconAllowOverlap(true),
      iconIgnorePlacement(true)
    );
    return wayPointLayer;
  }
}