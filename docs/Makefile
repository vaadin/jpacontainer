all: images

################################################################################
################################################################################

# List of images to be converted from SVG to PNG
IMAGES =	three-layer-architecture architecture \
			download-annotated \
			application-class \
			crudview-annotated addressbookmainview-annotated addressbookmainview-class \
			domain-model domain-model-department

################################################################################
# Image conversions
################################################################################

# SVG source images
SRCIMAGES := $(foreach file, $(IMAGES), original-drawings/$(file).svg)

# Two size of target images
TRGIMAGES_HI := $(foreach file, $(IMAGES), img/$(file)-hi.png)
TRGIMAGES_LO := $(foreach file, $(IMAGES), img/$(file)-lo.png)

images: $(TRGIMAGES_HI) $(TRGIMAGES_LO) FORCE

$(TRGIMAGES_HI): img/%-hi.png: original-drawings/%.svg
	inkscape --export-png $@ --export-dpi=300 --export-area-drawing $<

$(TRGIMAGES_LO): img/%-lo.png: original-drawings/%.svg
	inkscape --export-png $@ --export-dpi=90 --export-area-drawing $<

################################################################################
# Misc
################################################################################

FORCE:
