package domain;

public class Medicine implements Comparable<Medicine> {
    // Enkapsulasi ketat: semua atribut di-set private
    private final String medicineId;
    private String name;
    private String description;
    private double price;
    private int stock;
    private boolean requiresPrescription;

    // Constructor
    public Medicine(String medicineId, String name, String description, double price, int stock, boolean requiresPrescription) {
        this.medicineId = medicineId;
        this.name = name;
        this.description = description;
        setPrice(price); // Menggunakan setter untuk memicu validasi harga
        setStock(stock); // Menggunakan setter untuk memicu validasi stok awal
        this.requiresPrescription = requiresPrescription;
    }

    // =========================================
    // GETTER (Akses data read-only dari luar)
    // =========================================
    public String getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }

    // =========================================
    // SETTER DENGAN VALIDASI KETAT
    // =========================================
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Harga obat tidak boleh negatif.");
        }
        this.price = price;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stok obat tidak boleh kurang dari 0.");
        }
        this.stock = stock;
    }

    // =========================================
    // BUSINESS LOGIC & VALIDASI STOK
    // =========================================
    
    // Mengecek apakah stok masih tersedia sesuai jumlah yang diminta
    public boolean isInStock(int qty) {
        return this.stock >= qty;
    }

    // Mengurangi stok dengan custom exception jika stok tidak cukup
    public void reduceStock(int qty) throws OutOfStockException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan stok harus lebih dari 0.");
        }
        if (!isInStock(qty)) {
            throw new OutOfStockException("Stok tidak mencukupi untuk obat: " + this.name + ". Sisa stok saat ini: " + this.stock);
        }
        this.stock -= qty;
    }

    // =========================================
    // IMPLEMENTASI COMPARABLE (Sorting)
    // =========================================
    
    // Mengurutkan obat berdasarkan nama secara alfabetis (A-Z)
    @Override
    public int compareTo(Medicine other) {
        // Menggunakan compareToIgnoreCase agar pengurutan tidak sensitif huruf besar/kecil
        return this.name.compareToIgnoreCase(other.name);
    }
}
