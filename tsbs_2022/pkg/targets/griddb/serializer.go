package griddb

import (
	"github.com/timescale/tsbs/pkg/data"
	"github.com/timescale/tsbs/pkg/data/serialize"
	"io"
    "fmt"
)

// Serializer writes a Point in a serialized form for MongoDB
type Serializer struct{}


func (s *Serializer) Serialize(p *data.Point, w io.Writer) error {
	// Tag row first, prefixed with name 'tags'
	buf := make([]byte, 0, 256)
	buf = append(buf, []byte("tags")...)
	for _, v := range p.TagKeys() {
        value := v
		buf = append(buf, ',')
		buf = append(buf, value...)
	}
	_, err := w.Write(buf)
	if err != nil {
		return err
	}

	// Field row second
	buf = make([]byte, 0, 256)
	buf = append(buf, p.MeasurementName()...)
	buf = append(buf, ',')
	buf = append(buf, []byte(fmt.Sprintf("%d", p.Timestamp().UTC().UnixNano()))...)

    for _, v := range p.FieldValues() {
        value := v
		buf = append(buf, ',')
		buf = serialize.FastFormatAppend(value, buf)
	}
	buf = append(buf, '\n')

	for _, v := range p.FieldValues() {
        value := v
		buf = append(buf, ',')
		buf = serialize.FastFormatAppend(value, buf)
	}
	buf = append(buf, '\n')
	_, err = w.Write(buf)
	return err
}
