package com.wgu.capstone;

import com.wgu.capstone.entity.Part;
import com.wgu.capstone.entity.PartSupplier;
import com.wgu.capstone.entity.Supplier;
import com.wgu.capstone.repository.PartRepository;
import com.wgu.capstone.repository.PartSupplierRepository;
import com.wgu.capstone.repository.SupplierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PartRepository partRepository;
    private final SupplierRepository supplierRepository;
    private final PartSupplierRepository partSupplierRepository;

    public DataLoader(PartRepository partRepository,
                      SupplierRepository supplierRepository,
                      PartSupplierRepository partSupplierRepository) {
        this.partRepository = partRepository;
        this.supplierRepository = supplierRepository;
        this.partSupplierRepository = partSupplierRepository;
    }

    @Override
    public void run(String... args) {

        // ---- PARTS ----
        List<Part> parts = new ArrayList<>();
        
        // Washer Parts
        parts.add(new Part("WPW10123456", "Washer Drain Pump"));
        parts.add(new Part("WPW10123457", "Washer Door Lock"));
        parts.add(new Part("WPW10123458", "Washer Water Inlet Valve"));
        parts.add(new Part("WPW10123459", "Washer Lid Switch"));
        parts.add(new Part("WPW10123460", "Washer Drive Belt"));
        
        // Refrigerator Parts
        parts.add(new Part("DA97-12609C", "Refrigerator Water Filter"));
        parts.add(new Part("DA97-12610A", "Refrigerator Door Gasket"));
        parts.add(new Part("DA97-12611B", "Refrigerator Ice Maker Assembly"));
        parts.add(new Part("DA97-12612C", "Refrigerator Thermostat"));
        parts.add(new Part("DA97-12613D", "Refrigerator Evaporator Fan Motor"));
        
        // Dryer Parts
        parts.add(new Part("DC97-14486A", "Dryer Heating Element"));
        parts.add(new Part("DC97-14487B", "Dryer Thermal Fuse"));
        parts.add(new Part("DC97-14488C", "Dryer Belt"));
        parts.add(new Part("DC97-14489D", "Dryer Door Switch"));
        parts.add(new Part("DC97-14490E", "Dryer Blower Wheel"));
        
        // Dishwasher Parts
        parts.add(new Part("WD21X10025", "Dishwasher Circulation Pump"));
        parts.add(new Part("WD21X10026", "Dishwasher Door Latch"));
        parts.add(new Part("WD21X10027", "Dishwasher Spray Arm"));
        parts.add(new Part("WD21X10028", "Dishwasher Float Switch"));
        parts.add(new Part("WD21X10029", "Dishwasher Heating Element"));
        
        // Oven/Range Parts
        parts.add(new Part("WB27X10030", "Oven Bake Element"));
        parts.add(new Part("WB27X10031", "Oven Broil Element"));
        parts.add(new Part("WB27X10032", "Oven Temperature Sensor"));
        parts.add(new Part("WB27X10033", "Range Igniter"));
        parts.add(new Part("WB27X10034", "Range Surface Burner"));
        
        // Save all parts
        partRepository.saveAll(parts);

        // ---- SUPPLIERS ----
        List<Supplier> suppliers = new ArrayList<>();
        
        Supplier supplier1 = new Supplier("AppliancePartsPros", 3);  // Fast shipping
        Supplier supplier2 = new Supplier("RepairClinic", 5);           // Medium shipping
        Supplier supplier3 = new Supplier("PartSelect", 7);             // Slower shipping
        Supplier supplier4 = new Supplier("AppliancePartsDirect", 2);   // Fastest shipping
        Supplier supplier5 = new Supplier("ReliableParts", 6);         // Medium-slow shipping
        
        suppliers.add(supplier1);
        suppliers.add(supplier2);
        suppliers.add(supplier3);
        suppliers.add(supplier4);
        suppliers.add(supplier5);
        
        supplierRepository.saveAll(suppliers);

        // ---- PART_SUPPLIERS ----
        // Create multiple supplier options for each part to enable comparison
        List<PartSupplier> partSuppliers = new ArrayList<>();
        
        // For each part, create 5 supplier options with varying prices and stock
        int partIndex = 0;
        for (Part part : parts) {
            String partId = part.getPartId();
            
            // Determine base price based on part type
            BigDecimal basePrice = determineBasePrice(partId);
            
            // Create supplier options with price variations
            // Use partIndex to create variation in stock levels
            int stockVariation = (partIndex % 5) * 3;
            
            // Supplier 1 (AppliancePartsPros) - Usually mid-range price, good stock
            partSuppliers.add(new PartSupplier(
                supplier1, 
                part, 
                basePrice.multiply(new BigDecimal("1.00")), 
                12 + stockVariation
            ));
            
            // Supplier 2 (RepairClinic) - Usually slightly higher, medium stock
            partSuppliers.add(new PartSupplier(
                supplier2, 
                part, 
                basePrice.multiply(new BigDecimal("1.10")), 
                8 + stockVariation
            ));
            
            // Supplier 3 (PartSelect) - Usually cheapest, variable stock
            partSuppliers.add(new PartSupplier(
                supplier3, 
                part, 
                basePrice.multiply(new BigDecimal("0.90")), 
                15 + stockVariation
            ));
            
            // Supplier 4 (AppliancePartsDirect) - Premium price, excellent stock
            partSuppliers.add(new PartSupplier(
                supplier4, 
                part, 
                basePrice.multiply(new BigDecimal("1.15")), 
                20 + stockVariation
            ));
            
            // Supplier 5 (ReliableParts) - Competitive price, good stock
            partSuppliers.add(new PartSupplier(
                supplier5, 
                part, 
                basePrice.multiply(new BigDecimal("0.95")), 
                10 + stockVariation
            ));
            
            partIndex++;
        }
        
        partSupplierRepository.saveAll(partSuppliers);
        
        System.out.println("Data loaded successfully:");
        System.out.println("  - Parts: " + parts.size());
        System.out.println("  - Suppliers: " + suppliers.size());
        System.out.println("  - Part-Supplier relationships: " + partSuppliers.size());
    }
    
    private BigDecimal determineBasePrice(String partId) {
        // Assign base prices based on part type/category
        if (partId.startsWith("WPW")) {
            // Washer parts - $40-$60 range
            return new BigDecimal("50.00");
        } else if (partId.startsWith("DA97")) {
            // Refrigerator parts - $25-$45 range
            return new BigDecimal("35.00");
        } else if (partId.startsWith("DC97")) {
            // Dryer parts - $30-$50 range
            return new BigDecimal("40.00");
        } else if (partId.startsWith("WD21")) {
            // Dishwasher parts - $35-$55 range
            return new BigDecimal("45.00");
        } else if (partId.startsWith("WB27")) {
            // Oven/Range parts - $20-$40 range
            return new BigDecimal("30.00");
        } else {
            // Default price
            return new BigDecimal("40.00");
        }
    }
}