package griddb

import (
	"github.com/blagojts/viper"
	"github.com/spf13/pflag"
	"github.com/timescale/tsbs/pkg/data/serialize"
	"github.com/timescale/tsbs/pkg/data/source"
	"github.com/timescale/tsbs/pkg/targets"
	"github.com/timescale/tsbs/pkg/targets/constants"
)

func NewTarget() targets.ImplementedTarget {
	return &griddbTarget{}
}

type griddbTarget struct {
}

func (t *griddbTarget) TargetSpecificFlags(flagPrefix string, flagSet *pflag.FlagSet) {
	flagSet.String(flagPrefix+"url", "http://localhost:9000/", "GridDB REST end point")
	flagSet.String(flagPrefix+"ilp-bind-to", "127.0.0.1:9009", "GridDB griddb line protocol TCP ip:port")
}

func (t *griddbTarget) TargetName() string {
	return constants.FormatGridDB
}

func (t *griddbTarget) Serializer() serialize.PointSerializer {
	return &Serializer{}
}

func (t *griddbTarget) Benchmark(string, *source.DataSourceConfig, *viper.Viper) (targets.Benchmark, error) {
	panic("not implemented")
}
