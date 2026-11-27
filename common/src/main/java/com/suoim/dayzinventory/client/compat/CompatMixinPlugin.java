/*
 * DayZ Inventory
 * Copyright 2026 suoim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.suoim.dayzinventory.client.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Decides which optional-compatibility mixins apply.
 * <p>
 * It runs before any mod has finished loading, so it cannot ask the loader
 * which mods are present. It looks for a class the other mod ships instead, as
 * a resource, so nothing is loaded early. It lives outside the mixin package
 * because classes in a mixin package may not be loaded as ordinary classes.
 */
public class CompatMixinPlugin implements IMixinConfigPlugin {
    private static final String MOUSE_TWEAKS_API = "yalter/mousetweaks/api/IMTModGuiContainer3Ex.class";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".MouseTweaksCompatMixin")) {
            return hasResource(MOUSE_TWEAKS_API);
        }
        return true;
    }

    private static boolean hasResource(String path) {
        ClassLoader loader = CompatMixinPlugin.class.getClassLoader();
        if (loader != null && loader.getResource(path) != null) {
            return true;
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        return context != null && context.getResource(path) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
