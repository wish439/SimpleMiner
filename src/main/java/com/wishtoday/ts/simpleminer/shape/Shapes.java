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
    private final List<Shape> sortedShapes;
    @CreateConstruction
    public Shapes(List<Shape> shapes) {
        Int2ObjectOpenHashMap<Shape> map = new Int2ObjectOpenHashMap<>();
        for (Shape shape : shapes) {
            map.put(shape.index(), shape);
        }
        this.shapes = map;
        this.sortedShapes = new ArrayList<>();
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

    public void refreshShapesCache() {
        this.sortedShapes.clear();
        ArrayList<Shape> list = new ArrayList<>(this.shapes.values());
        list.sort(Comparator.comparingInt(Shape::index));
        this.sortedShapes.addAll(list);
    }

    public List<Shape> sortedShapes() {
        if (sortedShapes.isEmpty()) {
            this.refreshShapesCache();
        }
        return sortedShapes;
    }
}
