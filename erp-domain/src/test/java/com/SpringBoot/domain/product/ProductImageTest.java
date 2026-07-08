package com.SpringBoot.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductImageTest {

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/img.png", "https://example.com/images/laptop.jpg"})
    void of_aceptaUrlAbsolutaHttpOHttps(String url) {
        assertThat(ProductImage.of(url).imageUrl()).isEqualTo(url);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "/relative/path.png", "example.com/image.png"})
    void of_rechazaUrlInvalidaOBlank(String url) {
        assertThatThrownBy(() -> ProductImage.of(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rechazaValorNulo() {
        assertThatThrownBy(() -> ProductImage.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFileName_extraeElNombreDelArchivoDeLaUrl() {
        ProductImage image = ProductImage.of("https://example.com/images/laptop-pro.jpg");

        assertThat(image.getFileName()).isEqualTo("laptop-pro.jpg");
    }

    @Test
    void getFullUrl_devuelveLaUrlCompleta() {
        ProductImage image = ProductImage.of("https://example.com/images/laptop-pro.jpg");

        assertThat(image.getFullUrl()).isEqualTo("https://example.com/images/laptop-pro.jpg");
    }
}
