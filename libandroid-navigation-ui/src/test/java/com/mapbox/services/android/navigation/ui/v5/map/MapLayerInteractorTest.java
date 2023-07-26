package com.mapbox.services.android.navigation.ui.v5.map;

import vn.vietmap.vietmapsdk.maps.VietMapGL;
import vn.vietmap.vietmapsdk.maps.Style;
import vn.vietmap.vietmapsdk.style.layers.CircleLayer;
import vn.vietmap.vietmapsdk.style.layers.HeatmapLayer;
import vn.vietmap.vietmapsdk.style.layers.Layer;
import vn.vietmap.vietmapsdk.style.layers.LineLayer;
import vn.vietmap.vietmapsdk.style.layers.Property;
import vn.vietmap.vietmapsdk.style.layers.PropertyValue;
import vn.vietmap.vietmapsdk.style.layers.SymbolLayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static vn.vietmap.vietmapsdk.style.layers.PropertyFactory.visibility;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapLayerInteractorTest {

  @Test
  public void updateLayerVisibility_visibilityIsSet() {
    LineLayer anySymbolOrLineLayer = mock(LineLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "any");

    verify(anySymbolOrLineLayer).setProperties(any(PropertyValue.class));
  }

  @Test
  public void updateLayerVisibility_visibilityIsNotSet() {
    SymbolLayer anySymbolOrLineLayer = mock(SymbolLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "random");

    verify(anySymbolOrLineLayer, times(0)).setProperties(any(PropertyValue.class));
  }

  @Test
  public void updateLayerVisibility_visibilityIsNotSetIfInvalidLayer() {
    CircleLayer invalidLayer = mock(CircleLayer.class);
    List<Layer> layers = buildLayerListWith(invalidLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    layerInteractor.updateLayerVisibility(true, "circle");

    verify(invalidLayer, times(0)).setProperties(any(PropertyValue.class));
  }

  @Test
  public void isLayerVisible_visibleReturnsTrue() {
    SymbolLayer anySymbolOrLineLayer = mock(SymbolLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    when(anySymbolOrLineLayer.getVisibility()).thenReturn(visibility(Property.VISIBLE));
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("any");

    assertTrue(isVisible);
  }

  @Test
  public void isLayerVisible_visibleReturnsFalse() {
    LineLayer anySymbolOrLineLayer = mock(LineLayer.class);
    when(anySymbolOrLineLayer.getSourceLayer()).thenReturn("any");
    when(anySymbolOrLineLayer.getVisibility()).thenReturn(visibility(Property.NONE));
    List<Layer> layers = buildLayerListWith(anySymbolOrLineLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("any");

    assertFalse(isVisible);
  }

  @Test
  public void isLayerVisible_visibleReturnsFalseIfInvalidLayer() {
    HeatmapLayer invalidLayer = mock(HeatmapLayer.class);
    List<Layer> layers = buildLayerListWith(invalidLayer);
    VietMapGL map = mock(VietMapGL.class);
    when(map.getStyle()).thenReturn(mock(Style.class));
    when(map.getStyle().getLayers()).thenReturn(layers);
    MapLayerInteractor layerInteractor = new MapLayerInteractor(map);

    boolean isVisible = layerInteractor.isLayerVisible("heatmap");

    assertFalse(isVisible);
  }

  private List<Layer> buildLayerListWith(Layer layerToAdd) {
    List<Layer> layers = new ArrayList<>();
    layers.add(layerToAdd);
    return layers;
  }
}