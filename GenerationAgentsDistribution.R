df <- read.table("position_agents_norep_clean.data")

require(ggplot2)

p <- ggplot(df, aes(df$V1,df$V2)) + labs(x = "X") + labs(y = "Y") + labs(title = "Distribució agents DMASON")
p <- p + stat_bin2d(bins = 20)
pdf("AgentsDistribDMASON.pdf")
plot(p)
dev.off()
