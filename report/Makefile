MAKEFLAGS += --no-builtin-rules
LATEX = pdflatex -interaction=nonstopmode
BIBER = biber
SOURCES = main.tex
TARGETS = $(SOURCES:%.tex=%.pdf)

.PHONY: all depend clean
.SUFFIXES:

all: $(TARGETS)

mostlyclean:
	rm -f $(SOURCES:%.tex=%.aux)
	rm -f $(SOURCES:%.tex=%.log)
	rm -f $(SOURCES:%.tex=%.out)
	rm -f $(SOURCES:%.tex=%.nav)
	rm -f $(SOURCES:%.tex=%.bcf)
	rm -f $(SOURCES:%.tex=%.bbl)
	rm -f $(SOURCES:%.tex=%.blg)
	rm -f $(SOURCES:%.tex=%.run.xml)
	rm -f $(SOURCES:%.tex=%.toc)
	rm -f $(SOURCES:%.tex=%.snm)
	rm -f $(SOURCES:%.tex=%.synctex.gz)

clean: mostlyclean
	rm -f $(TARGETS)

main.tex: $(wildcard *.tex)

%.pdf : %.tex
	$(LATEX) $<
	$(LATEX) $<

inotify: all
	( while true; do \
	  inotifywait -e modify -e moved_to -e create ${SOURCES} >/dev/null 2>&1; \
	  $(MAKE) -C . ;\
	  done; \
	)
