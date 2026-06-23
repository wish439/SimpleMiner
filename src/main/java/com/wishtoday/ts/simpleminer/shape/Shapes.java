package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class Shapes {
    private final List<Shape> shapes;
    @CreateConstruction
    public Shapes(List<Shape> shapes) {
        this.shapes = new ArrayList<>(shapes);
        this.shapes.sort(Comparator.comparingInt(Shape::index));
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
}
