import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

const InvoiceTable = ({ data }) => {
	return (
		<Table>
			<TableCaption>A list of your recent invoices.</TableCaption>
			<TableHeader>
				<TableRow>
					<TableHead className="w-[100px]">Time</TableHead>
					<TableHead>Distribution channel</TableHead>
					<TableHead>Product Category</TableHead>
					<TableHead>Region</TableHead>
					<TableHead className="text-right">Revenue ($M)</TableHead>
					<TableHead className="text-right">Costs ($M)</TableHead>
					<TableHead className="text-right">Units Sold</TableHead>
					<TableHead className="text-right">Average Sale Price ($)</TableHead>
					<TableHead className="text-right">Discounts Given ($)</TableHead>
					<TableHead className="text-right">Returns ($)</TableHead>
					<TableHead className="text-right">Customer Satisfaction Rating</TableHead>
					<TableHead className="text-right">Marketing Spend ($)</TableHead>
				</TableRow>
			</TableHeader>
			<TableBody>
				{data.map((invoice, index) => (
					<TableRow key={index}>
						<TableCell className="font-medium">{invoice.Time}</TableCell>
						<TableCell>{invoice['Distribution channel']}</TableCell>
						<TableCell>{invoice['Product Category']}</TableCell>
						<TableCell>{invoice.Region}</TableCell>
						<TableCell className="text-right">{invoice['Revenue ($M)']}</TableCell>
						<TableCell className="text-right">{invoice['Costs ($M)']}</TableCell>
						<TableCell className="text-right">{invoice['Units Sold']}</TableCell>
						<TableCell className="text-right">{invoice['Average Sale Price ($)']}</TableCell>
						<TableCell className="text-right">{invoice['Discounts Given ($)']}</TableCell>
						<TableCell className="text-right">{invoice['Returns ($)']}</TableCell>
						<TableCell className="text-right">{invoice['Customer Satisfaction Rating']}</TableCell>
						<TableCell className="text-right">{invoice['Marketing Spend ($)']}</TableCell>
					</TableRow>
				))}
			</TableBody>
		</Table>
	)
}

export default InvoiceTable