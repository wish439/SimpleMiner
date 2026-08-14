package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class Shapes {
    private final Int2ObjectOpenHashMap<Shape> shapes;
    @CreateConstruction
    public Shapes(List<Shape> shapes) {
        Int2ObjectOpenHashMap<Shape> map = new Int2ObjectOpenHashMap<>();
        for (Shape shape : shapes) {
            map.put(shape.index(), shape);
        }
        this.shapes = map;
    }
    public Shape getFromIndex(int index) {
        if (index < 0) {
            return null;
        }
        if (index >= shapes.size()) {
            return null;
        }
        return shapes.get(index);
    }

    public int getShapeCount() {
        return shapes.size();
    }
}
