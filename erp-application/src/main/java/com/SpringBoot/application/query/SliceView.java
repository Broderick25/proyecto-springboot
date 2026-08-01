package com.SpringBoot.application.query;

import java.util.List;

public record SliceView<T>(List<T> content, int page, int size, boolean hasNext) {
}
