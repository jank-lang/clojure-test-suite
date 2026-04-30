<?php
// Override Phel's default 'tests' TestDirs path
// Reference: https://phel-lang.org/documentation/configuration/
return (new \Phel\Config\PhelConfig())
    ->setSrcDirs(['src'])
    ->setTestDirs(['test']);
