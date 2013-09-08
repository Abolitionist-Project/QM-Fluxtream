SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

-- -----------------------------------------------------
-- Table `qm-variable-categories`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-variable-categories` (
  `id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL COMMENT 'Name of the category' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
CHECKSUM = 1
PACK_KEYS = DEFAULT;


-- -----------------------------------------------------
-- Table `qm-unit-categories`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-unit-categories` (
  `id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(32) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `qm-units`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-units` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(16) NOT NULL ,
  `abbreviated-name` VARCHAR(16) NOT NULL ,
  `category` TINYINT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) ,
  INDEX `fk_unitCategory` (`category` ASC) ,
  UNIQUE INDEX `abbr_name_UNIQUE` (`abbreviated-name` ASC) ,
  CONSTRAINT `fk_unitCategory`
    FOREIGN KEY (`category` )
    REFERENCES `qm-unit-categories` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `qm-variables`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-variables` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `user` BIGINT UNSIGNED NOT NULL ,
  `name` VARCHAR(64) NOT NULL COMMENT 'Name of the variable' ,
  `variable-category` TINYINT UNSIGNED NULL COMMENT 'Category of the variable' ,
  `default-unit` SMALLINT UNSIGNED NOT NULL COMMENT 'ID of the default unit of measurement to use for this variable' ,
  `combination-operation` TINYINT UNSIGNED NOT NULL COMMENT 'How to combine values of this variable (for instance, to see a summary of the values over a month)\n\n0 - sum\n1 - mean' ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`user` ASC, `name` ASC) ,
  INDEX `fk_variableCategory` (`variable-category` ASC) ,
  INDEX `fk_variableDefaultUnit` (`default-unit` ASC) ,
  CONSTRAINT `fk_variableCategory`
    FOREIGN KEY (`variable-category` )
    REFERENCES `qm-variable-categories` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_variableDefaultUnit`
    FOREIGN KEY (`default-unit` )
    REFERENCES `qm-units` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
CHECKSUM = 1
PACK_KEYS = DEFAULT;


-- -----------------------------------------------------
-- Table `qm-measurement-sources`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-measurement-sources` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(32) NOT NULL COMMENT 'Name of the measurement source' ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) )
ENGINE = InnoDB
CHECKSUM = 1
INSERT_METHOD = NO
PACK_KEYS = DEFAULT;


-- -----------------------------------------------------
-- Table `qm-measurements`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-measurements` (
  `user` BIGINT UNSIGNED NOT NULL COMMENT 'ID of user that owns this measurement' ,
  `variable` INT UNSIGNED NOT NULL COMMENT 'ID of variable measured' ,
  `source` SMALLINT UNSIGNED NOT NULL COMMENT 'ID of measurement source' ,
  `timestamp` INT UNSIGNED NOT NULL COMMENT 'Time that this measurement occurred\n\nUses epoch minute (epoch time divided by 60)' ,
  `value` DOUBLE NOT NULL COMMENT 'Value measured' ,
  `unit` SMALLINT UNSIGNED NOT NULL COMMENT 'ID of unit of measurement' ,
  PRIMARY KEY (`user`, `variable`, `source`, `timestamp`) ,
  INDEX `fk_measurementVariable` (`variable` ASC) ,
  INDEX `fk_measurementSource` (`source` ASC) ,
  INDEX `fk_measurementUnits` (`unit` ASC) ,
  CONSTRAINT `fk_measurementVariable`
    FOREIGN KEY (`variable` )
    REFERENCES `qm-variables` (`id` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `fk_measurementSource`
    FOREIGN KEY (`source` )
    REFERENCES `qm-measurement-sources` (`id` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `fk_measurementUnits`
    FOREIGN KEY (`unit` )
    REFERENCES `qm-units` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
PACK_KEYS = DEFAULT;


-- -----------------------------------------------------
-- Table `qm-variable-user-settings`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-variable-user-settings` (
  `user` BIGINT UNSIGNED NOT NULL COMMENT 'The user whose preferences these are' ,
  `variable` INT UNSIGNED NOT NULL COMMENT 'The variable these preferences are for' ,
  `unit` SMALLINT UNSIGNED NULL COMMENT 'The unit to use for this variable' ,
  PRIMARY KEY (`user`, `variable`) ,
  INDEX `fk_variableSettings` (`variable` ASC) ,
  INDEX `fk_userVariableUnit` (`unit` ASC) ,
  CONSTRAINT `fk_variableSettings`
    FOREIGN KEY (`variable` )
    REFERENCES `qm-variables` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_userVariableUnit`
    FOREIGN KEY (`unit` )
    REFERENCES `qm-units` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `qm-unit-conversions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qm-unit-conversions` (
  `unit` SMALLINT UNSIGNED NOT NULL ,
  `step-number` TINYINT UNSIGNED NOT NULL ,
  `operation` TINYINT UNSIGNED NOT NULL ,
  `value` DOUBLE NOT NULL ,
  PRIMARY KEY (`unit`, `step-number`) ,
  INDEX `fk_conversionUnit` (`unit` ASC) ,
  CONSTRAINT `fk_conversionUnit`
    FOREIGN KEY (`unit` )
    REFERENCES `qm-units` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
