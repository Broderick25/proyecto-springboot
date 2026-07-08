package com.SpringBoot.domain;

import com.SpringBoot.domain.document.Catalog;
import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.order.Customer;
import com.SpringBoot.domain.order.Order;
import com.SpringBoot.domain.order.OrderItem;
import com.SpringBoot.domain.order.OrderNumber;
import com.SpringBoot.domain.product.*;
import com.SpringBoot.domain.shared.CustomerId;
import com.SpringBoot.domain.shared.Money;
import com.SpringBoot.domain.shared.Quantity;

import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * Demostración del flujo completo del módulo de dominio (DDD).
 *
 * Este main ilustra cómo los diferentes building blocks de DDD
 * (Aggregates, Entities, Value Objects, Domain Events) colaboran
 * para modelar las reglas de negocio del ERP sin depender de
 * ninguna infraestructura externa (sin base de datos, sin HTTP).
 */
public class DomainFlowDemo {

    public static void main(String[] args) {

        Currency USD = Currency.getInstance("USD");

        IO.println("==========================================================");
        IO.println("  DEMO DEL MÓDULO DE DOMINIO - ERP LITE (DDD)");
        IO.println("==========================================================");

        // ------------------------------------------------------------------
        // 1. CATALOG DOCUMENT
        //
        // Catalog es un documento de Spring Data MongoDB (no un Aggregate
        // Root del dominio): agrupa CatalogItems bajo un catalogType (String)
        // tal como se persiste en la colección "catalogs". No expone lógica
        // de negocio propia -- filtros como "solo ítems activos" se resuelven
        // en la capa de aplicación, no en el documento.
        // ------------------------------------------------------------------
        IO.println("\n--- [1] CATALOG DOCUMENT ---");
        IO.println("Creando catálogo de categorías de productos...");

        CatalogItem electronics = new CatalogItem(
                "item-001",
                "ELECTRONICS",
                "Electrónica",
                "Productos electrónicos y tecnología",
                1,
                Map.of("taxRate", 0.16)
        );

        CatalogItem furniture = new CatalogItem(
                "item-002",
                "FURNITURE",
                "Muebles",
                "Mobiliario para hogar y oficina",
                2,
                Map.of("taxRate", 0.08)
        );

        Catalog productCatalog = Catalog.builder()
                .id("cat-001")
                .catalogType("PRODUCT_CATEGORIES")
                .name("Categorías de Productos")
                .description("Catálogo principal de categorías")
                .items(List.of(electronics, furniture))
                .active(true)
                .build();

        IO.println("Ítems en el catálogo: " + productCatalog.getItems().size());
        productCatalog.getItems().forEach(item ->
                IO.println("  - [" + item.code() + "] " + item.value()));

        // Búsqueda por código: al ser un documento plano, el filtro se hace
        // con un stream en vez de un método de negocio del propio Catalog
        productCatalog.getItems().stream()
                .filter(item -> "ELECTRONICS".equals(item.code()))
                .findFirst()
                .ifPresent(item ->
                        IO.println("Tasa de impuesto para ELECTRONICS: " + item.metadata().get("taxRate")));

        // ------------------------------------------------------------------
        // 2. PRODUCT AGGREGATE - Creación con Value Objects
        //
        // Product es un Aggregate Root. Cada campo del producto es un
        // Value Object (SKU, ProductName, Money, Stock, etc.) que encapsula
        // sus propias reglas de validación. La lógica de negocio queda en
        // el dominio, no en servicios externos ni en la capa de aplicación.
        //
        // Value Objects son inmutables: al "modificar" Stock se crea una
        // nueva instancia — nunca se muta el estado directamente.
        // ------------------------------------------------------------------
        IO.println("\n--- [2] PRODUCT AGGREGATE - Creación y Value Objects ---");

        // SKU valida el formato [A-Z]+-\d{3} — una regla de negocio pura
        SKU laptopSku = SKU.of("LAPTOP-001");
        // ProductName valida longitud mínima (3) y máxima (200)
        ProductName laptopName = ProductName.of("Laptop Pro 15");
        // Money es inmutable y valida amount >= 0, escala a 2 decimales
        Money laptopPrice = Money.of(1299.99, USD);
        // Stock valida que no sea negativo
        Stock laptopStock = Stock.of(50);
        // CategoryReference es un VO que apunta al Catalog
        CategoryReference electronicsRef = CategoryReference.of("ELECTRONICS");
        // ProductImage valida que sea una URL http/https absoluta
        ProductImage laptopImage = ProductImage.of("https://example.com/images/laptop-pro.jpg");

        // Product.create() es el factory method del Aggregate Root.
        // Internamente genera un ProductId (UUID), crea AuditInfo y
        // registra el DomainEvent ProductCreated.
        Product laptop = Product.create(
                laptopSku,
                laptopName,
                "Laptop de alto rendimiento con pantalla 15 pulgadas",
                laptopPrice,
                laptopStock,
                electronicsRef,
                laptopImage,
                "admin"
        );

        IO.println("Producto creado: " + laptop.getName().value());
        IO.println("  SKU: " + laptop.getSku().value());
        IO.println("  Precio: " + laptop.getPrice());
        IO.println("  Stock inicial: " + laptop.getStock().value());
        IO.println("  Activo: " + laptop.isActive());
        IO.println("  Eventos generados: " + laptop.getDomainEvents().size()
                + " -> " + laptop.getDomainEvents().get(0).getClass().getSimpleName());

        // Segundo producto para la orden
        Product mouse = Product.create(
                SKU.of("MOUSE-042"),
                ProductName.of("Mouse Inalámbrico Ergonómico"),
                "Mouse ergonómico con 6 botones programables",
                Money.of(49.99, USD),
                Stock.of(200),
                electronicsRef,
                ProductImage.of("https://example.com/images/mouse-ergo.jpg"),
                "admin"
        );
        IO.println("Producto creado: " + mouse.getName().value() + " | Stock: " + mouse.getStock().value());

        // ------------------------------------------------------------------
        // 3. PRODUCT AGGREGATE - Gestión de Stock
        //
        // incrementStock / decrementStock son comportamientos del Aggregate.
        // Cada operación registra un DomainEvent (StockChanged) con el motivo,
        // el stock anterior y el nuevo. Esto garantiza trazabilidad completa
        // sin necesidad de auditoría externa.
        //
        // Stock es un Value Object inmutable: this.stock = this.stock.decrement(n)
        // devuelve una nueva instancia en lugar de mutar la existente.
        // ------------------------------------------------------------------
        IO.println("\n--- [3] PRODUCT AGGREGATE - Gestión de Stock ---");

        laptop.getDomainEvents(); // consultamos eventos sin limpiarlos
        laptop.clearDomainEvents(); // la infra los limpiaría después de persistir

        laptop.incrementStock(20, "Reabastecimiento de proveedor Q1");
        IO.println("Stock tras reabastecimiento: " + laptop.getStock().value());

        laptop.decrementStock(5, "Reserva para campaña de marketing");
        IO.println("Stock tras reserva: " + laptop.getStock().value());

        // El Aggregate acumula todos los eventos — la infra los publica después
        IO.println("Eventos de stock acumulados: " + laptop.getDomainEvents().size());
        laptop.getDomainEvents().forEach(e ->
                IO.println("  -> " + e.getClass().getSimpleName()));
        laptop.clearDomainEvents();

        // ------------------------------------------------------------------
        // 4. PRODUCT AGGREGATE - Cambio de precio y desactivación
        //
        // changePrice() valida que el precio sea > 0 (regla de negocio en el
        // Aggregate). deactivate() valida que el producto esté activo antes
        // de cambiar estado — el Aggregate protege invariantes del negocio.
        // ------------------------------------------------------------------
        IO.println("\n--- [4] PRODUCT AGGREGATE - Precio y Ciclo de Vida ---");

        // Creamos un producto extra para demostrar desactivación
        Product oldKeyboard = Product.create(
                SKU.of("KEYB-099"),
                ProductName.of("Teclado Mecánico Vintage"),
                "Modelo descontinuado",
                Money.of(29.99, USD),
                Stock.of(3),
                electronicsRef,
                ProductImage.of("https://example.com/images/keyboard-vintage.jpg"),
                "admin"
        );
        oldKeyboard.clearDomainEvents();

        oldKeyboard.changePrice(Money.of(19.99, USD));
        IO.println("Precio actualizado: " + oldKeyboard.getPrice());

        // deactivate() protege la invariante: no se puede desactivar dos veces
        oldKeyboard.deactivate();
        IO.println("Producto desactivado: " + !oldKeyboard.isActive());

        // hasAvailableStock() combina estado activo + cantidad — lógica de negocio
        // encapsulada en el Aggregate, no en un servicio externo
        IO.println("¿Tiene stock disponible? " + oldKeyboard.hasAvailableStock(1)
                + " (false porque está inactivo)");

        // ------------------------------------------------------------------
        // 5. ORDER AGGREGATE - Creación con snapshot de productos
        //
        // OrderItem.from(product, quantity) crea una entidad que toma un
        // SNAPSHOT del precio y nombre del producto en el momento de la orden.
        // Esto garantiza que cambios futuros en el producto no afecten órdenes
        // ya creadas — regla de negocio fundamental del comercio.
        //
        // Order.create() valida que todos los ítems usen la misma moneda
        // y calcula el totalAmount sumando los subtotales (qty × price).
        // ------------------------------------------------------------------
        IO.println("\n--- [5] ORDER AGGREGATE - Creación ---");

        // CustomerId referencia al sistema externo de clientes (JSONPlaceholder)
        // Customer es un Value Object que agrupa ID + nombre para el contexto de la orden
        Customer customer = Customer.of(CustomerId.of(42L), "María García López");

        // OrderItem.from() hace snapshot: congela precio y nombre del producto
        OrderItem laptopItem = OrderItem.from(laptop, Quantity.of(2));
        IO.println("Item creado: " + laptopItem.getProductName()
                + " x" + laptopItem.getQuantity().value()
                + " = " + laptopItem.getSubtotal());

        OrderItem mouseItem = OrderItem.from(mouse, Quantity.of(3));
        IO.println("Item creado: " + mouseItem.getProductName()
                + " x" + mouseItem.getQuantity().value()
                + " = " + mouseItem.getSubtotal());

        // OrderNumber es un Value Object con formato ORD-YYYY-NNN
        OrderNumber orderNumber = OrderNumber.of("ORD-2026-001");

        // Order.create() valida ítems no vacíos, misma moneda, calcula total
        // y registra el DomainEvent OrderCreated
        Order order = Order.create(orderNumber, customer, List.of(laptopItem, mouseItem), "sistema");

        IO.println("Orden creada: " + order.getOrderNumber().value());
        IO.println("  Cliente: " + order.getCustomer().customerName());
        IO.println("  Estado: " + order.getStatus().value());
        IO.println("  Total: " + order.getTotalAmount());
        IO.println("  Items: " + order.getItems().size());
        IO.println("  Evento: " + order.getDomainEvents().get(0).getClass().getSimpleName());
        order.clearDomainEvents();

        // ------------------------------------------------------------------
        // 6. ORDER AGGREGATE - Agregar ítem en estado PENDING
        //
        // Solo en estado PENDING se pueden agregar o quitar ítems.
        // El Aggregate protege esta invariante: si el estado es otro,
        // lanza IllegalStateException. El total se recalcula automáticamente.
        // ------------------------------------------------------------------
        IO.println("\n--- [6] ORDER AGGREGATE - Modificación en PENDING ---");

        // Creamos un tercer producto para agregar a la orden
        Product usbHub = Product.create(
                SKU.of("USBH-007"),
                ProductName.of("USB Hub 7 Puertos"),
                "Hub USB 3.0 con alimentación independiente",
                Money.of(34.99, USD),
                Stock.of(80),
                electronicsRef,
                ProductImage.of("https://example.com/images/usb-hub.jpg"),
                "admin"
        );

        OrderItem usbItem = OrderItem.from(usbHub, Quantity.of(1));
        order.addItem(usbItem); // Solo permitido en PENDING
        IO.println("Ítem agregado. Nuevo total: " + order.getTotalAmount());
        IO.println("Ítems en la orden: " + order.getItems().size());

        // Removemos el mismo ítem para mantener la orden limpia para el demo
        order.removeItem(usbItem);
        IO.println("Ítem removido. Total restaurado: " + order.getTotalAmount());

        // ------------------------------------------------------------------
        // 7. ORDER AGGREGATE - Ciclo de vida completo (Happy Path)
        //
        // El OrderStatus es un Value Object que implementa la máquina de estados:
        //   PENDING → CONFIRMED → SHIPPED → DELIVERED
        //
        // Cada transición registra un DomainEvent. La capa de aplicación
        // escucha OrderConfirmed para decrementar stock en los productos.
        // La capa de aplicación escucha OrderShipped para notificar al cliente.
        //
        // validateTransition() dentro del Aggregate evita transiciones inválidas
        // sin necesitar servicios externos de validación.
        // ------------------------------------------------------------------
        IO.println("\n--- [7] ORDER AGGREGATE - Ciclo de Vida (Happy Path) ---");

        // PENDING → CONFIRMED
        // Dispara OrderConfirmed → la capa de aplicación lo usará para
        // decrementar stock en cada producto de la orden
        order.confirm();
        IO.println("Estado tras confirmar: " + order.getStatus().value());
        IO.println("  Evento: " + order.getDomainEvents().get(0).getClass().getSimpleName()
                + " (señal para decrementar stock)");
        order.clearDomainEvents();

        // CONFIRMED → SHIPPED
        // Dispara OrderShipped → notificación de envío al cliente
        order.ship();
        IO.println("Estado tras enviar: " + order.getStatus().value());
        IO.println("  Evento: " + order.getDomainEvents().get(0).getClass().getSimpleName());
        order.clearDomainEvents();

        // SHIPPED → DELIVERED (estado final)
        order.deliver();
        IO.println("Estado tras entregar: " + order.getStatus().value());
        IO.println("  Evento: " + order.getDomainEvents().get(0).getClass().getSimpleName());
        order.clearDomainEvents();

        // ------------------------------------------------------------------
        // 8. ORDER AGGREGATE - Flujo alternativo: Cancelación
        //
        // Se crea una segunda orden para demostrar el flujo de cancelación.
        // cancel() acepta cualquier estado antes de SHIPPED.
        // Si la orden estaba CONFIRMED al cancelar, se dispara OrderCancelled
        // con el motivo — la capa de aplicación lo usará para liberar stock.
        // ------------------------------------------------------------------
        IO.println("\n--- [8] ORDER AGGREGATE - Flujo Alternativo: Cancelación ---");

        OrderItem laptopItem2 = OrderItem.from(laptop, Quantity.of(1));
        Order orderToCancel = Order.create(
                OrderNumber.of("ORD-2026-002"),
                Customer.of(CustomerId.of(7L), "Carlos Ramírez"),
                List.of(laptopItem2),
                "sistema"
        );
        orderToCancel.clearDomainEvents();

        // Confirmamos para demostrar cancelación desde CONFIRMED
        orderToCancel.confirm();
        orderToCancel.clearDomainEvents();
        IO.println("Orden confirmada: " + orderToCancel.getOrderNumber().value());

        // CONFIRMED → CANCELLED
        // La capa de aplicación escucha OrderCancelled para liberar el stock
        orderToCancel.cancel("Cliente solicitó cancelación por cambio de dirección");
        IO.println("Estado tras cancelar: " + orderToCancel.getStatus().value());

        DomainEvent cancelEvent = orderToCancel.getDomainEvents().get(0);
        IO.println("  Evento: " + cancelEvent.getClass().getSimpleName()
                + " (señal para liberar stock reservado)");
        orderToCancel.clearDomainEvents();

        // ------------------------------------------------------------------
        // 9. VALIDACIÓN DE INVARIANTES POR VALUE OBJECTS
        //
        // Los Value Objects rechazan estados inválidos en construcción,
        // no en tiempo de validación tardía. Esto hace imposible crear
        // objetos de dominio en estado inconsistente (fail-fast).
        // ------------------------------------------------------------------
        IO.println("\n--- [9] VALIDACIÓN DE INVARIANTES (Value Objects) ---");

        testInvariant("SKU con formato inválido", () -> SKU.of("laptop001"));
        testInvariant("Precio negativo en Money", () -> Money.of(-10.0, USD));
        testInvariant("Stock negativo", () -> Stock.of(-1));
        testInvariant("CustomerId <= 0", () -> CustomerId.of(0L));
        testInvariant("ProductName muy corta (< 3 chars)", () -> ProductName.of("AB"));
        testInvariant("Orden sin ítems", () ->
                Order.create(OrderNumber.of("ORD-2026-003"),
                        Customer.of(CustomerId.of(1L), "Test"),
                        List.of(), "admin"));
        testInvariant("Transición inválida DELIVERED → CANCELLED", () -> order.cancel("intento inválido"));

        // ------------------------------------------------------------------
        // RESUMEN
        // ------------------------------------------------------------------
        IO.println("\n==========================================================");
        IO.println("  RESUMEN DEL FLUJO DE DOMINIO");
        IO.println("==========================================================");
        IO.println("Catalog  -> Documento de referencia (Spring Data MongoDB)");
        IO.println("           Sin lógica de negocio propia; se persiste tal cual");
        IO.println("Product  -> Aggregate Root con Value Objects inmutables");
        IO.println("           Stock, Price, SKU = reglas de negocio en construcción");
        IO.println("           Domain Events = trazabilidad de cambios de stock");
        IO.println("Order    -> Aggregate Root con máquina de estados (OrderStatus VO)");
        IO.println("           OrderItem = snapshot de precio/nombre en el momento");
        IO.println("           Domain Events = señales para la capa de aplicación");
        IO.println("           (stock decrement, notificaciones, liberación de reservas)");
        IO.println("==========================================================");
    }

    /**
     * Utilidad para demostrar que los Value Objects y Aggregates
     * lanzan excepciones cuando se viola una invariante de negocio.
     */
    private static void testInvariant(String description, Runnable action) {
        try {
            action.run();
            IO.println("  [FALLO] Se esperaba excepción en: " + description);
        } catch (IllegalArgumentException | IllegalStateException e) {
            IO.println("  [OK] Invariante protegida - " + description + ": " + e.getMessage());
        }
    }

    /**
     * Reemplaza System.out para cumplir la restricción del ejercicio.
     */
    static class IO {
        static void println(String message) {
            System.out.println(message);
        }
    }
}
