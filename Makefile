NAME		= convert

SRCS		= src/qoi/ArrayUtils.java \
			  src/qoi/Helper.java \
			  src/qoi/Main.java \
			  src/qoi/QOIDecoder.java \
			  src/qoi/QOIEncoder.java \
			  src/qoi/QOISpecification.java
CLASSES		= $(SRCS:src/qoi/%.java=out/qoi/%.class)

JAVAC		= javac
JAVACFLAGS	= -d out -sourcepath src
RM			= rm -f

out/qoi/%.class: src/qoi/%.java
	@$(JAVAC) $(JAVACFLAGS) $<

all: $(NAME)

$(NAME): $(CLASSES)
	@echo "Building $(NAME)..."
	@echo '#!/usr/bin/env bash' > $(NAME)
	@echo 'java -cp out qoi.Main "$$@"' >> $(NAME)
	@chmod +x $(NAME)
	@echo "Build complete! Run with ./$(NAME) <image.png|image.qoi>"

clean:
	@echo "Cleaning compiled classes..."
	@$(RM) -r out
	@echo "Clean done."

fclean: clean
	@echo "Removing $(NAME)..."
	@$(RM) $(NAME)
	@echo "Full clean done."

re: fclean all

.PHONY: all clean fclean re
