package com.SpringBoot.domain.document;

import org.springframework.data.mongodb.core.mapping.Field;

public record ProductSpecifications(

        // -- Electronics: laptops / tablets ----------------------------------
        @Field("processor")   String processor,
        @Field("ram")         String ram,
        @Field("storage")     String storage,
        @Field("display")     String display,
        @Field("weight")      String weight,

        // -- Electronics: monitors -------------------------------------------
        @Field("size")        String size,
        @Field("resolution")  String resolution,
        @Field("panel")       String panel,
        @Field("connectivity") String connectivity,
        @Field("colorGamut")  String colorGamut,

        // -- Electronics: keyboards ------------------------------------------
        @Field("switches")    String switches,
        @Field("layout")      String layout,
        @Field("frame")       String frame,
        @Field("backlight")   String backlight,

        // -- Electronics: mouse ----------------------------------------------
        @Field("dpi")         String dpi,
        @Field("buttons")     String buttons,
        @Field("battery")     String battery,

        // -- Electronics: printer / webcam -----------------------------------
        @Field("type")        String type,
        @Field("speed")       String speed,
        @Field("duplex")      String duplex,
        @Field("fps")         String fps,
        @Field("microphone")  String microphone,
        @Field("autofocus")   String autofocus,

        // -- Electronics: dock -----------------------------------------------
        @Field("interface")      String interfaceStandard,
        @Field("ports")          Integer ports,
        @Field("powerDelivery")  String powerDelivery,
        @Field("videoOutput")    String videoOutput,

        // -- Electronics: cables ---------------------------------------------
        @Field("length")         String length,
        @Field("standard")       String standard,
        @Field("dataTransfer")   String dataTransfer,
        @Field("charging")       String charging,
        @Field("compatibility")  String compatibility,

        // -- Furniture: desk -------------------------------------------------
        @Field("dimensions")     String dimensions,
        @Field("heightRange")    String heightRange,
        @Field("weightCapacity") String weightCapacity,
        @Field("material")       String material,
        @Field("motor")          String motor,

        // -- Furniture: chair ------------------------------------------------
        @Field("brand")          String brand,
        @Field("model")          String model,
        @Field("adjustable")     String adjustable,
        @Field("maxWeight")      String maxWeight,

        // -- Furniture: lamp -------------------------------------------------
        @Field("brightness")     String brightness,
        @Field("colorTemp")      String colorTemp,
        @Field("usbPort")        String usbPort,

        // -- Furniture: organizer / whiteboard -------------------------------
        @Field("compartments")   Integer compartments,
        @Field("cableManagement") String cableManagement,
        @Field("surface")        String surface,
        @Field("includes")       String includes,
        @Field("mounting")       String mounting,

        // -- Accessories: backpack -------------------------------------------
        @Field("capacity")       String capacity,
        @Field("laptopSize")     String laptopSize,
        @Field("color")          String color,

        // -- Accessories: artificial plant -----------------------------------
        @Field("height")         String height,
        @Field("maintenance")    String maintenance,

        // -- Stationery: notebook --------------------------------------------
        @Field("pages")          Integer pages,
        @Field("ruling")         String ruling,
        @Field("cover")          String cover,

        // -- Stationery: pens ------------------------------------------------
        @Field("quantity")       Integer quantity,
        @Field("tipSize")        String tipSize,
        @Field("inkColor")       String inkColor,
        @Field("refillable")     String refillable,

        // -- Electronics: headset --------------------------------------------
        @Field("noiseCancelling") String noiseCancelling
) {}
