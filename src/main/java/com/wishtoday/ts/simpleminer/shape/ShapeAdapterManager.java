package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShapeAdapterManager {
    private final Map<Class<? extends Shape>, ShapeAdapter> shapeAdapters;
    private final Shapes shapes;

    public ShapeAdapterManager(List<ShapeAdapter> shapeAdapters, Shapes shapes) {
        this.shapes = shapes;
        Map<Class<? extends Shape>, ShapeAdapter> map = new HashMap<>();
        for (ShapeAdapter adapter : shapeAdapters) {
            map.put(adapter.supportedShape(), adapter);
        }
        this.shapeAdapters = new HashMap<>(map);
    }

    @Nullable
    public ShapeAdapter getShapeAdapter(Class<? extends Shape> shapeClass) {
        return shapeAdapters.get(shapeClass);
    }

    @Nullable
    public ShapeAdapter getShapeAdapter(int shapeIndex) {
        Shape shape = this.shapes.getFromIndex(shapeIndex);
        if (shape == null) {
            return null;
        }
        return shapeAdapters.get(shape.getClass());
    }
}
