<?php
// Override Phel's default 'tests' TestDirs path
// Reference: https://phel-lang.org/documentation/configuration/
return Phel\Config\PhelConfig::forProject()
    ->withSrcDirs(['src'])
    ->withTestDirs(['test']);
