var JavaPackages = new JavaImporter(
    Packages.ray.rage.scene.SceneManager,
    Packages.ray.rage.scene.Light,
    Packages.ray.rage.scene.Light.Type,
    Packages.ray.rage.scene.Light.Type.SPOT,
    Packages.java.awt.Color
);

with (JavaPackages) {
    var positionalLight = sceneManager.createLight("PositionalLight", Light.Type.SPOT);
    positionalLight.setAmbient(new Color(0.3, 0.3, 0.3));
    positionalLight.setDiffuse(new Color(0.7, 0.7, 0.7));
    positionalLight.setSpecular(new Color(1.0, 1.0, 1.0));
    positionalLight.setRange(10);
}